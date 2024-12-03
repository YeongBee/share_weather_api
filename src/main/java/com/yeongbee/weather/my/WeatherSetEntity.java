package com.yeongbee.weather.my;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WeatherSetEntity {

    private long rnStAm;    // 오전 강수 확률
    private long rnStPm;    // 오후 강수 확률

    private long taMin;     // 최저기온
    private long taMax;     // 최고기온

    private String wfAm;    // AM 날씨
    private String wfPm;    // PM 날씨

    public WeatherSetEntity(long rnStAm, long rnStPm, long taMin, long taMax, String wfAm, String wfPm) {
        this.rnStAm = rnStAm;
        this.rnStPm = rnStPm;
        this.taMin = taMin;
        this.taMax = taMax;
        this.wfAm = wfAm;
        this.wfPm = wfPm;
    }
}