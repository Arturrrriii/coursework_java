package metallurgy;

import java.nio.file.*;
public class Tests {
    static void check(boolean x,String message){if(!x)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        var x=new ConverterCalculator.Input(200,1.6,3.2,4,.6,30,1.15);
        var r=ConverterCalculator.calculate(x);
        check(Math.abs(r.diameter()-5.5075)<.01,"диаметр по объему усеченного конуса");
        check(Math.abs(r.freeHeight()-6.56)<.05,"высота свободного пространства");
        try{ConverterCalculator.calculate(new ConverterCalculator.Input(0,1,3,4,.6,30,1.15));throw new AssertionError("нет ошибки ввода");}catch(IllegalArgumentException ok){}
        var pc=ProcessChecks.calculate(x,r,new ProcessChecks.Input(490,.0554,1.3,293,2000,1.43,16,48.6,6.1,48,1,1.7));
        check(Math.abs(pc.speed()-327.3)<1,"скорость струи");
        check(Math.abs(pc.inclinedPenetration()-1.2)<.05,"глубина струи");
        check(pc.minDepth()<1.6 && pc.maxMixDepth()>1.6,"ограничения глубины");
        byte[] salt=PasswordUtil.salt(), hash=PasswordUtil.hash("password123".toCharArray(),salt);
        check(PasswordUtil.verify("password123".toCharArray(),salt,hash),"проверка пароля");
        check(!PasswordUtil.verify("other123".toCharArray(),salt,hash),"неверный пароль");
        Path db=Files.createTempFile("metallurgy-test-", ".sqlite");
        try(Database d=new Database(db.toString())){d.register("test_user","password123".toCharArray()); long id=d.login("test_user","password123".toCharArray()); for(int i=0;i<100;i++)d.insert(id,x); check(d.list(id).size()==100,"100 записей");long record=d.list(id).get(0).id();d.delete(id,record);check(d.list(id).size()==99,"удаление");}
        finally{Files.deleteIfExists(db);}
        System.out.println("Все проверки пройдены");
    }
}
