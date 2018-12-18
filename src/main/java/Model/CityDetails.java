package Model;

/**
 * This class saves all the details of the cities in the program
 */
public class CityDetails {

    public String countryName;
    public String currency;
    public String population;

    public CityDetails(String countryName, String currencies, String population) {
        this.countryName = countryName;
        this.currency = currencies;
        this.population = population;
    }


    //<editor-fold desc="Getters">

    public String getCountryName() {
        return countryName;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPopulation() {
        return population;
    }

    //</editor-fold>

    //<editor-fold desc="Setters">


    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    //</editor-fold>


    @Override
    public String toString() {
        return countryName + "," + currency + "," + population;
    }
}
