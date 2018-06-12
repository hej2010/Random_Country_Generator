package se.swecookie.randomcountrygenerator;

import android.support.annotation.NonNull;

class Country {
    private final String name;
    private final String continent;
    private final String code;

    /**
     * Creates a Country object
     *
     * @param name      the name of the country
     * @param continent the continent
     * @param code      the country code
     */
    private Country(@NonNull String name, @NonNull String continent, @NonNull String code) {
        this.name = name;
        this.continent = continent;
        this.code = code;
    }

    String getName() {
        return name;
    }

    String getContinent() {
        return continent;
    }

    String getCode() {
        return code;
    }

    static Country stringToCountry(@NonNull String xml) {
        // <country code="AL" iso="8" continent="EU">Albania</country>
        String name = xml.split(">")[1].split("<")[0];
        String continent = xml.split("\"")[3];
        String code = xml.split("\"")[1];
        return new Country(name, continent, code);
    }

    @Override
    public String toString() {
        return name + ", " + continent + ", " + code;
    }
}
