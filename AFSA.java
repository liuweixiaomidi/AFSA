import java.util.Date;

public class AFSA {
    private final int FishNumber; //鱼群数目
    private final int TryTimes; //尝试次数
    private final int dim; //维度
    private final int step; //人工鱼移动步长
    private final double delta; //拥挤度因子
    private final int visual; //视野范围
    //人工鱼群、范围内最佳鱼，遍历时的下一条鱼
    Fish[] fish;
    Fish BestFish;
    Fish[] NextFish;
    int index; //遍历索引
    double[][] vector;
    private final int[] chosen;
    //范围内鱼群数目 fishCount
    //public int ScopeLength;

    public AFSA(int FishNumber, int TryTimes, int dim, int step, double delta, int visual)
    {
        this.FishNumber = FishNumber;
        this.TryTimes = TryTimes;
        this.dim = dim;
        this.step = step;
        this.delta = delta;
        this.visual = visual;
        fish = new Fish[FishNumber];
        NextFish = new Fish[3];
        vector = new double[FishNumber][dim];
        chosen = new int[FishNumber];
        index = 0;
        init();
    }

    public void DoAFSA(int num)
    {
        long StartTime = new Date().getTime();
        //double a = 0;
        int count = 1;
        //int len = 0;
        while (count <= num)
        {
            for (int i = 0; i < FishNumber; i++)
            {
                prey(i);
                swarm(i);
                follow(i);
                bulletin(i);
                System.out.println("第" + count + "遍第" + i + "条鱼结束");
            }
            System.out.println(count + "当前最优值：" + BestFish.fit);
            for (int i = 0; i < dim; i++)
            {
                System.out.println("位置" + (i + 1) + ": " + BestFish.x[i]);
            }
            System.out.println();
            count++;
            System.out.println("step:" + step + " visual:" + visual);
        }
        System.out.println("最优值：" + BestFish.fit);
        for (int i = 0; i < dim; i++)
        {
            System.out.println("位置" + (i + 1) + ": " + BestFish.x[i]);
        }
        System.out.println();
        long EndTime = new Date().getTime();
        System.out.println("本程序运行计时: " + (EndTime-StartTime) + " 毫秒。");
    }

    /*评价行为*/
    private void bulletin(int i)
    {
        Fish MaxFish;
        MaxFish = NextFish[0];
        for (int j = 0; j < 3; j++)
        {
            if (NextFish[j].fit > MaxFish.fit && NextFish[j].x[0] != 0 && NextFish[j].x[1] != 0)
            {
                MaxFish = NextFish[j];
            }
        }
        if (MaxFish.fit < fish[i].fit)
        {
            return;
        }
        fish[i] = MaxFish;
        if (MaxFish.fit > BestFish.fit)
        {
            BestFish = MaxFish;
        }
    }

     /*
     * 追尾行为
     * 人工鱼探索周围邻居鱼的最优位置，
     * 当最优位置的目标函数值大于当前位置的目标函数值并且不是很拥挤，则当前位置向最优邻居鱼移动一步，
     * 否则执行觅食行为。
     */
    private void follow(int i)
    {
        NextFish[2] = new Fish(dim, visual);
        Fish MaxFish;
        MaxFish = fish[i];
        //获得视野范围内的鱼群
        Fish[] scope = getScopeFish(i);
        int key = i;
        if (scope != null)
        {
            for (int j = 0; j < scope.length; j++)
            {
                //最大适应度的鱼
                if (scope[j].fit > MaxFish.fit)
                {
                    MaxFish = scope[j];
                    key = j;
                }
            }
            //如果最小适应度的鱼也比自己大，就去觅食
            if (MaxFish.fit <= fish[i].fit)
            {
                prey(i);
            }
            else
            {
                Fish[] newScope = getScopeFish(key);
                if (newScope != null)
                {
                    //检查拥挤度，能不能插入，不能插入就去觅食
                    if (newScope.length * MaxFish.fit < delta * fish[i].fit)
                    {
                        double dis = fish[i].distance(MaxFish);
                        //如果能够插入，就往MinFish的位置移动
                        for (int k = 0; k < dim; k++)
                        {
                            NextFish[2].x[k] = (int) (fish[i].x[k] + (MaxFish.x[k] - fish[i].x[k]) * step * Math.random() / dis);
                        }
                        //更新适应度
                        NextFish[2].fit = NextFish[2].NewFunction(NextFish[2].x);
                    } else prey(i);
                } else prey(i);
            }
        }
        else prey(i);
    }

     /*
     * 人工鱼i的聚群行为
     * 人工鱼探索当前邻居内的伙伴数量，并计算伙伴的中心位置，
     * 然后把新得到的中心位置的目标函数与当前位置的目标函数相比较，
     * 如果中心位置的目标函数优于当前位置的目标函数并且不是很拥挤，则当前位置向中心位置移动一步，否则执行觅食行为。
     * 鱼聚群时会遵守两条规则：一是尽量向邻近伙伴的中心移动，二是避免过分拥挤。
     */
    private void swarm(int i)
    {
        NextFish[1] = new Fish(dim,visual);
        int[] center = new int[dim];
        for (int j = 0; j < dim; j++)
        {
            center[j] = 0;
        }
        //取得视野内的鱼群
        Fish[] scope = getScopeFish(i);
        if (scope != null)
        {
            for (Fish value : scope)
            {
                for (i = 0; i < dim; ++i)
                {
                    center[i] += value.x[i];
                }
            }
            //计算中心位置
            for( i = 0; i < dim; i++ )
            {
                center[i] /= scope.length;
            }
            //满足条件
            double dis;
            Fish CenterFish = new Fish(dim,visual);
            CenterFish.x = center;
            CenterFish.fit = CenterFish.NewFunction(CenterFish.x);
            dis = fish[i].distance(CenterFish);
            if (CenterFish.fit > fish[i].fit && scope.length * CenterFish.fit < delta * fish[i].fit)
            {
                for (int j = 0; j < dim; j++)
                {
                    NextFish[1].x[j] = (int) (fish[i].x[j] + (CenterFish.x[j] - fish[i].x[j]) * step * Math.random() / dis);
                }
                NextFish[1].fit = NextFish[1].NewFunction(NextFish[1].x);
            }
            else
            {
                prey(i);
            }
        }
        else
        {
            prey(i);
        }
    }

     /*人工鱼i的觅食行为*/
    private void prey(int i)
    {
        Fish NewFish = new Fish(dim,visual);
        NewFish.fit = 0;
        NextFish[0] = new Fish(dim,visual);
        //选择次数达到一定数量后，如果仍然不满足条件，则随机移动一步
        for (int k = 0; k <TryTimes; k++ )
        {           // 进行try_number次尝试
            //在其感知范围内随机选择另一个状态
            for (int j = 0; j < dim; j++)
            {
                NewFish.x[j] = (int) ((2 * (Math.random()) - 1) * visual);
            }
            NewFish.fit = NewFish.NewFunction(NewFish.x);
            //如果得到的状态的目标函数大于当前的状态，则向新选择得到的状态靠近一步
            if( NewFish.fit > fish[i].fit )
            {
                double dis = fish[i].distance(NewFish);
                for(int j = 0; j < dim; j++ )
                {
                    NextFish[0].x[j] = (int) (fish[i].x[j] + (NewFish.x[j] - fish[i].x[j]) * step * Math.random() / dis);
                }
                NextFish[0].fit = NextFish[0].NewFunction(NextFish[0].x);
            }
            else
            {
                //反之，重新选取新状态
                for (int j = 0; j < dim; j++)
                {
                    NextFish[0].x[j] = (int) (fish[i].x[j] + visual * (2 * (Math.random()) - 1));
                    NextFish[0].fit = NextFish[0].NewFunction(NextFish[0].x);
                }
            }
        }
    }

    /*获得鱼i视野范围内的鱼群*/
    private Fish[] getScopeFish(int i)
    {
        int num = 0;
        //计算视野范围内的鱼群个数，并且记录下标
        for (int j = 0; j < FishNumber; j++)
        {
            chosen[j] = -1;
            if (fish[i].distance(fish[j]) < visual)
            {
                chosen[j] = i;
                num++;
            }
        }
        //如果视野范围内有其它鱼，标记出来返回
        if (num != 0)
        {
            Fish[] scope = new Fish[num];
            int k = 0;
            for (int j = 0; j < FishNumber; j++)
            {
                if (chosen[j] != -1)
                {
                    scope[k++] = fish[chosen[j]];
                }
            }
            return scope;
        }
        return null;
    }

     /*
     * 初始化鱼群，随机生成鱼的位置
     * 并且根据位置计算其适应度
     */
    private void init()
    {
        for (int i = 0; i < FishNumber; i++)
        {
            fish[i] = new Fish(dim, visual);
            fish[i].fit = fish[i].NewFunction(fish[i].x);
        }
        //最优鱼群
        BestFish = new Fish(dim, visual);
        BestFish.fit = -9999999;
    }

}