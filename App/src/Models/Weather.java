package Models;

import jade.core.AID;

public class Weather extends Request{
    private int temperature;
    private int wind;
    private int humidity;
    private int precipitacion;
    private int[] location = new int[2];//Latitude|Longitude

    public Weather(int temperature, int wind, int humidity, int precipitacion, String identification, AID sender, AID receiver) {
        super(identification, sender, receiver,0);
        this.temperature = temperature;
        this.wind = wind;
        this.humidity = humidity;
        this.precipitacion = precipitacion;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getWind() {
        return wind;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPrecipitacion() {
        return precipitacion;
    }

    public int[] getLocation() {
        return location;
    }
       
    
}
