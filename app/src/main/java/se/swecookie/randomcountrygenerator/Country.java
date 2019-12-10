package se.swecookie.randomcountrygenerator;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import java.util.Locale;

class Country {
    @ColumnInfo(name = "name")
    private final String name;
    @ColumnInfo(name = "continent")
    private final String continent;
    @ColumnInfo(name = "code")
    private final String code;
    @Ignore
    private int drawableID;

    /**
     * Creates a Country object
     *
     * @param name      the name of the country
     * @param continent the continent
     * @param code      the country code
     */
    Country(@NonNull String name, @NonNull String continent, @NonNull String code) {
        this.name = name;
        this.continent = continent;
        this.code = code;
        this.drawableID = 0;
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

    @NonNull
    @Override
    public String toString() {
        return name + ", " + continent + ", " + code;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Country) {
            return ((Country) obj).code.equals(this.code);
        }
        return false;
    }

    public int getDrawableID(@NonNull Context context) {
        String c = code;
        if (c.equals("DO")) {
            c = "do1";
        }
        if (drawableID == 0) {
            drawableID = context.getResources().getIdentifier(c.toLowerCase(Locale.ENGLISH), "drawable", BuildConfig.APPLICATION_ID);
        }
        return drawableID;
    }
}
