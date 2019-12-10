package se.swecookie.randomcountrygenerator;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
class CountryHistory extends Country {
    @PrimaryKey
    private long date;

    /**
     * Creates a Country object
     *
     * @param name      the name of the country
     * @param continent the continent
     * @param code      the country code
     * @param date      the date of this couuntry being selected
     */
    CountryHistory(@NonNull String name, @NonNull String continent, @NonNull String code, long date) {
        super(name, continent, code);
        this.date = date;
    }


    long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
