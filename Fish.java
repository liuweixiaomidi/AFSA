public class Fish
{
    public int dim;
    public int visual;
    public int[] x;
    public double fit;

    public Fish(int dim, int visual)
    {
        this.dim = dim;
        this.visual = visual;
        x = new int[dim];
        for (int i = 0; i < dim; i++)
        {
            x[i] = (int) Math.floor(256 * Math.random());
        }
        fit = 0;
    }

    public double distance(Fish f)
    {
        double a = 0;
        for (int i = 0; i < dim; i++)
        {
            if (this.x[i] == f.x[i])
            {
                a = 0.00001;
            }
            else
            {
                a += (this.x[i] - f.x[i]) * (this.x[i] - f.x[i]);
            }
        }
        return Math.sqrt(a);
    }

    public double NewFunction(int[] w)
    {
        return -(w[0] * w[0] - 160 * w[1] + 640 * w[1] - 260 * w[1] + 16900);
    }
}
