package com.redpulse.app.data.repository;

import android.content.Context;

import com.redpulse.app.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddressRepository {
    private static final String ASSET_FILE = "bd_address_hierarchy.json";
    private static Catalog cachedCatalog;

    public Catalog getCatalog(Context context) throws Exception {
        if (cachedCatalog != null) return cachedCatalog;
        String json = readAsset(context);
        cachedCatalog = parse(json);
        return cachedCatalog;
    }

    private String readAsset(Context context) throws Exception {
        try (InputStream inputStream = context.getAssets().open(ASSET_FILE);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }

    private Catalog parse(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray divisionsArray = root.getJSONArray("divisions");
        List<Division> divisions = new ArrayList<>();
        for (int i = 0; i < divisionsArray.length(); i++) {
            JSONObject divisionObject = divisionsArray.getJSONObject(i);
            Division division = new Division();
            division.id = divisionObject.optString("id");
            division.name = divisionObject.optString("name");
            JSONArray districtsArray = divisionObject.optJSONArray("districts");
            if (districtsArray != null) {
                for (int j = 0; j < districtsArray.length(); j++) {
                    JSONObject districtObject = districtsArray.getJSONObject(j);
                    District district = new District();
                    district.id = districtObject.optString("id");
                    district.name = districtObject.optString("name");
                    district.zilaName = districtObject.optString("zilaName", district.name);
                    JSONArray cityCorporationsArray = districtObject.optJSONArray("cityCorporations");
                    district.cityCorporations.add(Constants.CITY_CORPORATION_NONE);
                    if (cityCorporationsArray != null) {
                        for (int k = 0; k < cityCorporationsArray.length(); k++) {
                            String value = cityCorporationsArray.optString(k);
                            if (value != null && !value.trim().isEmpty()) district.cityCorporations.add(value);
                        }
                    }
                    JSONArray upazilasArray = districtObject.optJSONArray("upazilas");
                    if (upazilasArray != null) {
                        for (int k = 0; k < upazilasArray.length(); k++) {
                            JSONObject upazilaObject = upazilasArray.getJSONObject(k);
                            Upazila upazila = new Upazila();
                            upazila.id = upazilaObject.optString("id");
                            upazila.name = upazilaObject.optString("name");
                            upazila.unions.add(Constants.UNION_NOT_APPLICABLE);
                            JSONArray unionsArray = upazilaObject.optJSONArray("unions");
                            if (unionsArray != null) {
                                for (int m = 0; m < unionsArray.length(); m++) {
                                    JSONObject unionObject = unionsArray.getJSONObject(m);
                                    String unionName = unionObject.optString("name");
                                    if (unionName != null && !unionName.trim().isEmpty()) {
                                        upazila.unions.add(unionName);
                                    }
                                }
                            }
                            district.upazilas.add(upazila);
                        }
                    }
                    division.districts.add(district);
                }
            }
            divisions.add(division);
        }
        return new Catalog(divisions);
    }

    public static class Catalog {
        private final List<Division> divisions;

        public Catalog(List<Division> divisions) {
            this.divisions = divisions;
        }

        public List<String> getDivisionNames() {
            List<String> names = new ArrayList<>();
            for (Division division : divisions) names.add(division.name);
            return names;
        }

        public Division findDivision(String name) {
            for (Division division : divisions) {
                if (division.name.equalsIgnoreCase(name)) return division;
            }
            return null;
        }
    }

    public static class Division {
        String id;
        String name;
        final List<District> districts = new ArrayList<>();

        public String getName() { return name; }
        public List<District> getDistricts() { return Collections.unmodifiableList(districts); }
    }

    public static class District {
        String id;
        String name;
        String zilaName;
        final List<String> cityCorporations = new ArrayList<>();
        final List<Upazila> upazilas = new ArrayList<>();

        public String getName() { return name; }
        public String getZilaName() { return zilaName; }
        public List<String> getCityCorporations() { return Collections.unmodifiableList(cityCorporations); }
        public List<Upazila> getUpazilas() { return Collections.unmodifiableList(upazilas); }
    }

    public static class Upazila {
        String id;
        String name;
        final List<String> unions = new ArrayList<>();

        public String getName() { return name; }
        public List<String> getUnions() { return Collections.unmodifiableList(unions); }
    }
}
