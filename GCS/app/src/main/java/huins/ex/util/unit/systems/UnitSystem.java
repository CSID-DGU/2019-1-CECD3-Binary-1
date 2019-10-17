package huins.ex.util.unit.systems;

import huins.ex.util.unit.providers.area.AreaUnitProvider;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.util.unit.providers.speed.SpeedUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public interface UnitSystem {

    public static final int AUTO = 0;
    public static final int METRIC = 1;
    public static final int IMPERIAL = 2;

    public LengthUnitProvider getLengthUnitProvider();

    public AreaUnitProvider getAreaUnitProvider();

    public SpeedUnitProvider getSpeedUnitProvider();

}
