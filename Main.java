public class Main
{
    public static void main(String[] args)
    {
        System.out.println("begin");
        AFSA run = new AFSA(10,5,2,5,0.2,10);
        run.DoAFSA(40 );
    }
}
