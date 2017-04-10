package android.projet.meteo;

/**
 * Created by zonia on 06/04/2017.
 */

public class Weather {

    public double id;
    public String main;
    public String description;
    public String icon;

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }



}
