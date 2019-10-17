package huins.ex.util.unit.systems;

import huins.ex.util.unit.providers.area.AreaUnitProvider;
import huins.ex.util.unit.providers.area.ImperialAreaUnitProvider;
import huins.ex.util.unit.providers.length.ImperialLengthUnitProvider;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.util.unit.providers.speed.ImperialSpeedUnitProvider;
import huins.ex.util.unit.providers.speed.SpeedUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public class ImperialUnitSystem implements UnitSystem{

    private static final LengthUnitProvider lengthUnitProvider = new ImperialLengthUnitProvider();
    private static final AreaUnitProvider areaUnitProvider = new ImperialAreaUnitProvider();
    private static final SpeedUnitProvider speedUnitProvider = new ImperialSpeedUnitProvider();

    @Override
    public LengthUnitProvider getLengthUnitProvider() {
        return lengthUnitProvider;
    }

    @Override
    public AreaUnitProvider getAreaUnitProvider() {
        return areaUnitProvider;
    }

    @Override
    public SpeedUnitProvider getSpeedUnitProvider() {
        return speedUnitProvider;
    }
}
