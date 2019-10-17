package huins.ex.util.unit.systems;

import huins.ex.util.unit.providers.area.AreaUnitProvider;
import huins.ex.util.unit.providers.area.MetricAreaUnitProvider;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.util.unit.providers.length.MetricLengthUnitProvider;
import huins.ex.util.unit.providers.speed.MetricSpeedUnitProvider;
import huins.ex.util.unit.providers.speed.SpeedUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public class MetricUnitSystem implements UnitSystem {

    private static final LengthUnitProvider lengthUnitProvider = new MetricLengthUnitProvider();
    private static final AreaUnitProvider areaUnitProvider = new MetricAreaUnitProvider();
    private static final SpeedUnitProvider speedUnitProvider = new MetricSpeedUnitProvider();

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
