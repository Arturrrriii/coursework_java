package metallurgy;

/** Учебная геометрическая модель кислородного конвертера, единицы СИ. */
public final class ConverterCalculator {
    private ConverterCalculator() {}
    public record Input(double mass, double depth, double intensity, int nozzles,
                        double manganese, double angle, double slagFactor) {}
    public record Result(double bathVolume, double diameter, double bottomDiameter,
                         double freeHeight, double height, double mouthDiameter,
                         double coneHeight, double cylinderHeight, double volume,
                         double specificVolume, double heightRatio, double maxDepth) {}
    public static Result calculate(Input x) {
        if (!Double.isFinite(x.mass) || !Double.isFinite(x.depth) || !Double.isFinite(x.intensity)
                || !Double.isFinite(x.manganese) || !Double.isFinite(x.angle) || !Double.isFinite(x.slagFactor))
            throw new IllegalArgumentException("Числа должны быть конечными");
        if (x.mass <= 0 || x.mass > 1000 || x.depth <= 0 || x.intensity <= 0 || x.nozzles < 1
                || x.nozzles > 30 || x.manganese < 0 || x.manganese > 3
                || x.angle < 25 || x.angle > 35 || x.slagFactor < 1.10 || x.slagFactor > 1.15)
            throw new IllegalArgumentException("Проверьте диапазоны: M 0–1000 т, глубина и q > 0, сопел 1–30, Mn 0–3 %, угол 25–35°, коэффициент 1,10–1,15");
        double maxDepth = 10000 * 1.4 / 7000;
        if (x.depth > maxDepth) throw new IllegalArgumentException("Глубина превышает 2 м при принятой нагрузке 1,4 кг/см² и плотности 7000 кг/м³");
        double vBath = 1000 * x.mass * x.slagFactor / 7000;
        double ratio = .855;
        double d = Math.sqrt(12 * vBath / (Math.PI * x.depth * (1 + ratio + ratio * ratio)));
        double k = x.manganese <= .55 ? 1.17 - .58 * x.manganese : .73 + .22 * x.manganese;
        double free = k * Math.pow(x.mass * x.intensity / x.nozzles, .4);
        double total = x.depth + free;
        double mouth = Math.max(.22 * Math.sqrt(x.mass), .12 * Math.sqrt(x.intensity * x.mass));
        if (mouth >= d) throw new IllegalArgumentException("Диаметр горловины получился не меньше диаметра корпуса. Измените исходные данные");
        double cone = (d - mouth) / (2 * Math.tan(Math.toRadians(x.angle)));
        double cyl = free - cone;
        if (cyl <= 0) throw new IllegalArgumentException("Высота цилиндрической части не положительна. Измените исходные данные");
        double vCyl = Math.PI * d * d * cyl / 4;
        double vCone = Math.PI * cone * (d*d + mouth*mouth + d*mouth) / 12;
        double vol = vBath + vCyl + vCone;
        return new Result(vBath,d,ratio*d,free,total,mouth,cone,cyl,vol,vol/x.mass,total/d,maxDepth);
    }
}
