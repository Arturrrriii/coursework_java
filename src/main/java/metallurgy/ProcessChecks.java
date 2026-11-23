package metallurgy;
/** Проверка проникновения струи и минимальной высоты цилиндра по выделению газа. */
public final class ProcessChecks {
    private ProcessChecks(){}
    public record Input(double nozzleSpeed,double nozzleDiameter,double distance,double jetTemperature,double gasTemperature,double gasDensity,double nozzleAngle,double co,double co2,double oxygen,double bubbleSpeed,double unevenness){}
    public record Result(double speed,double jetDiameter,double penetration,double inclinedPenetration,double minDepth,double maxMixDepth,double blowTime,double maxGasFlow,double minCylinder){}
    public static Result calculate(ConverterCalculator.Input x,ConverterCalculator.Result r,Input p){
        double[] a={p.nozzleSpeed,p.nozzleDiameter,p.distance,p.jetTemperature,p.gasTemperature,p.gasDensity,p.nozzleAngle,p.co,p.co2,p.oxygen,p.bubbleSpeed,p.unevenness};for(double v:a)if(!Double.isFinite(v))throw new IllegalArgumentException("Параметры должны быть конечными");
        if(p.nozzleSpeed<=0||p.nozzleDiameter<=0||p.distance<=0||p.jetTemperature<=0||p.gasTemperature<=0||p.gasDensity<=0||p.nozzleAngle<0||p.nozzleAngle>=90||p.co<0||p.co2<0||p.co+p.co2<=0||p.oxygen<=0||p.bubbleSpeed<=0||p.unevenness<1)throw new IllegalArgumentException("Проверьте положительность данных и угол 0–90°");
        double w=6*p.nozzleSpeed*p.nozzleDiameter/p.distance*Math.sqrt(p.gasTemperature/p.jetTemperature);
        double d=p.nozzleDiameter+p.distance/2;
        double h=Math.sqrt(p.gasDensity/(7000*9.8))*w*Math.sqrt(d);
        double hs=h*Math.cos(Math.toRadians(p.nozzleAngle));
        double time=p.oxygen/x.intensity();
        double vmax=p.unevenness*(p.co+p.co2)*x.mass()/(60*time);
        double denominator=Math.PI*p.bubbleSpeed*r.diameter()*r.diameter()-2*vmax;
        if(denominator<=0)throw new IllegalArgumentException("Невозможно вычислить высоту цилиндра: знаменатель ≤ 0; пересмотрите газовые данные");
        return new Result(w,d,h,hs,hs+.3,1.4*hs,time,vmax,2*x.depth()*vmax/denominator);
    }
}
