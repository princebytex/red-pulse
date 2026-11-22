const admin = require('firebase-admin');
const {onDocumentCreated, onDocumentUpdated} = require('firebase-functions/v2/firestore');

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

const REGISTRATION_BONUS = 1;
const REQUEST_CREDIT_COST = 1;

function haversineKm(lat1, lon1, lat2, lon2) {
  const toRad = (value) => (value * Math.PI) / 180;
  const earthRadius = 6371;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLon / 2) ** 2;
  return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

async function addNotification(targetUserId, payload) {
  if (!targetUserId) return;
  await db.collection('notifications').add({
    targetUserId,
    title: payload.title,
    body: payload.body,
    type: payload.type,
    referenceId: payload.referenceId || null,
    read: false,
    priority: payload.priority || 'normal',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

async function sendPushToUser(targetUserId, notification, data = {}) {
  if (!targetUserId) return;
  const devices = await db.collection('users').doc(targetUserId).collection('devices').get();
  const tokens = [];
  devices.forEach((device) => {
    const token = device.data()?.token;
    if (token) tokens.push(token);
  });
  for (const token of tokens) {
    await messaging.send({token, notification, data});
  }
}

exports.onUserProfileCreated = onDocumentCreated('users/{userId}', async (event) => {
  const user = event.data?.data();
  if (!user || user.role !== 'user') return;

  const monthKey = new Date().toISOString().slice(0, 7);
  await db.collection('credit_transactions').add({
    userId: event.params.userId,
    type: 'registration_bonus',
    points: REGISTRATION_BONUS,
    balanceAfter: user.totalCredits || REGISTRATION_BONUS,
    monthKey,
    createdBy: 'system',
    description: 'Welcome bonus on registration',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  const title = 'Welcome to Red Pulse';
  const body = 'Your account is ready and 1 credit bonus has been added automatically.';
  await addNotification(event.params.userId, {
    title,
    body,
    type: 'registration_bonus',
    referenceId: event.params.userId,
    priority: 'normal',
  });

  await sendPushToUser(event.params.userId, {title, body}, {
    type: 'registration_bonus',
    userId: event.params.userId,
  });
});

exports.onDonationInterestCreated = onDocumentCreated('donations/{donationId}', async (event) => {
  const donation = event.data?.data();
  if (!donation || donation.status !== 'interest_submitted') return;

  const title = `New donor interest for ${donation.requestBloodGroup || 'blood request'}`;
  const body = `${donation.donorName || 'A donor'} shared interest. Open your request to view contact details.`;

  await addNotification(donation.receiverId, {
    title,
    body,
    type: 'donor_interest',
    referenceId: donation.requestId,
    priority: 'high',
  });

  await sendPushToUser(donation.receiverId, {title, body}, {
    type: 'donor_interest',
    requestId: donation.requestId || '',
    donationId: event.params.donationId,
  });
});

exports.onRequestReviewed = onDocumentUpdated('blood_requests/{requestId}', async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();
  if (!after || !before) return;
  if (before.status === after.status && before.active === after.active) return;

  if (!after.requesterId) return;

  if (after.status === 'published' && after.active === true && (before.status !== 'published' || before.active !== true)) {
    const title = `Request approved: ${after.bloodGroup || 'blood request'}`;
    const body = 'Your request is now live for donors. Interested donors can start responding.';

    await addNotification(after.requesterId, {
      title,
      body,
      type: 'request_approved',
      referenceId: event.params.requestId,
      priority: after.emergency ? 'high' : 'normal',
    });

    await sendPushToUser(after.requesterId, {title, body}, {
      type: 'request_approved',
      requestId: event.params.requestId,
    });
  }

  if (after.status === 'rejected' && before.status !== 'rejected') {
    const title = `Request rejected: ${after.bloodGroup || 'blood request'}`;
    const body = after.adminDecisionReason || 'Please review the request details and submit again.';

    await addNotification(after.requesterId, {
      title,
      body,
      type: 'request_rejected',
      referenceId: event.params.requestId,
      priority: 'high',
    });

    await sendPushToUser(after.requesterId, {title, body}, {
      type: 'request_rejected',
      requestId: event.params.requestId,
    });
  }
});

exports.onEmergencyRequestPublished = onDocumentUpdated('blood_requests/{requestId}', async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();
  if (!after || !before) return;
  if (!after.emergency) return;
  if (!(after.status === 'published' && after.active === true)) return;
  if (before.status === 'published' && before.active === true) return;

  const settingsDoc = await db.collection('app_settings').doc('global').get();
  const radiusKm = settingsDoc.exists ? (settingsDoc.data().emergencyRadiusKm || 25) : 25;

  const donors = await db.collection('users')
    .where('bloodGroup', '==', after.bloodGroup)
    .where('available', '==', true)
    .where('blocked', '==', false)
    .where('deleted', '==', false)
    .get();

  for (const donorDoc of donors.docs) {
    const donor = donorDoc.data();
    if (!donor || donor.uid === after.requesterId) continue;

    let withinScope = true;
    let distanceLabel = '';
    if ([donor.latitude, donor.longitude, after.latitude, after.longitude].every((value) => value != null)) {
      const distanceKm = haversineKm(after.latitude, after.longitude, donor.latitude, donor.longitude);
      withinScope = distanceKm <= radiusKm;
      distanceLabel = ` • ${distanceKm.toFixed(1)} km away`;
    }
    if (!withinScope) continue;

    const title = `Emergency ${after.bloodGroup} needed`;
    const body = `${after.hospitalName || 'Hospital request'}${distanceLabel}`;

    await addNotification(donor.uid, {
      title,
      body,
      type: 'emergency_request',
      referenceId: event.params.requestId,
      priority: 'high',
    });

    await sendPushToUser(donor.uid, {title, body}, {
      type: 'emergency_request',
      requestId: event.params.requestId,
    });
  }
});

exports.onDonationStatusUpdated = onDocumentUpdated('donations/{donationId}', async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();
  if (!after || !before) return;

  if (before.status !== after.status && after.status === 'cancelled_by_requester') {
    const title = 'Your donor response was cancelled';
    const body = 'The requester closed your donor response for this blood request.';
    await addNotification(after.donorId, {
      title,
      body,
      type: 'donor_interest_cancelled',
      referenceId: after.requestId,
      priority: 'normal',
    });
    await sendPushToUser(after.donorId, {title, body}, {
      type: 'donor_interest_cancelled',
      requestId: after.requestId || '',
      donationId: event.params.donationId,
    });
  }

  if (before.creditsGranted || after.creditsGranted) return;
  if (!['confirmed_by_receiver', 'verified'].includes(after.status)) return;

  const monthKey = new Date().toISOString().slice(0, 7);
  await db.runTransaction(async (trx) => {
    const donationRef = db.collection('donations').doc(event.params.donationId);
    const donationSnap = await trx.get(donationRef);
    const donation = donationSnap.data();
    if (!donation || donation.creditsGranted) return;

    const donorRef = db.collection('users').doc(donation.donorId);
    const requesterRef = db.collection('users').doc(donation.receiverId);
    const donorSnap = await trx.get(donorRef);
    const requesterSnap = await trx.get(requesterRef);

    const donor = donorSnap.data();
    const requester = requesterSnap.data();
    if (!donor || !requester) throw new Error('User profile missing for credit transfer');
    if ((requester.totalCredits || 0) < REQUEST_CREDIT_COST) {
      throw new Error('Requester does not have enough credits to complete this transfer');
    }

    const requesterCredits = (requester.totalCredits || 0) - REQUEST_CREDIT_COST;
    const donorCredits = (donor.totalCredits || 0) + REQUEST_CREDIT_COST;

    trx.update(requesterRef, {
      totalCredits: requesterCredits,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    trx.update(donorRef, {
      totalCredits: donorCredits,
      lastDonationAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    trx.set(db.collection('credit_transactions').doc(), {
      userId: donation.receiverId,
      type: 'request_credit_used',
      points: -REQUEST_CREDIT_COST,
      balanceAfter: requesterCredits,
      sourceDonationId: event.params.donationId,
      sourceRequestId: donation.requestId,
      monthKey,
      createdBy: 'system',
      description: 'Credit transferred after confirmed blood donation',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    trx.set(db.collection('credit_transactions').doc(), {
      userId: donation.donorId,
      type: 'donation_credit_received',
      points: REQUEST_CREDIT_COST,
      balanceAfter: donorCredits,
      sourceDonationId: event.params.donationId,
      sourceRequestId: donation.requestId,
      monthKey,
      createdBy: 'system',
      description: 'Credit received for successful blood donation',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    trx.update(donationRef, {
      creditsGranted: true,
      creditTransferPoints: REQUEST_CREDIT_COST,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });

  const donorTitle = 'Donation confirmed';
  const donorBody = 'The requester marked blood given. 1 credit has been added to your account.';
  await addNotification(after.donorId, {
    title: donorTitle,
    body: donorBody,
    type: 'donation_reward',
    referenceId: after.requestId,
    priority: 'high',
  });
  await sendPushToUser(after.donorId, {title: donorTitle, body: donorBody}, {
    type: 'donation_reward',
    requestId: after.requestId || '',
  });

  const requesterTitle = 'Request completed';
  const requesterBody = 'This donation was marked complete and 1 credit was transferred to the donor.';
  await addNotification(after.receiverId, {
    title: requesterTitle,
    body: requesterBody,
    type: 'request_completed',
    referenceId: after.requestId,
    priority: 'normal',
  });
  await sendPushToUser(after.receiverId, {title: requesterTitle, body: requesterBody}, {
    type: 'request_completed',
    requestId: after.requestId || '',
  });
});
