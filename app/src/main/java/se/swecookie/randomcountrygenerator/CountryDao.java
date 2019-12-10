package se.swecookie.randomcountrygenerator;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CountryDao {
    @Query("SELECT * FROM countryhistory")
    List<CountryHistory> getAll();

    //@Query("SELECT * FROM countryhistory WHERE uid IN (:userIds)")
    //List<User> loadAllByIds(int[] userIds);

    //@Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    //User findByName(String first, String last);

    @Insert
    void insertAll(CountryHistory... histories);

    @Query("DELETE FROM countryhistory")
    public void nukeTable();

}
