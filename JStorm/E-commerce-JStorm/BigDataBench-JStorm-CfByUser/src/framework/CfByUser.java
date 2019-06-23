package framework;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import com.alibaba.jstorm.client.ConfigExtension;
import com.alibaba.jstorm.client.WorkerAssignment;
import framework.bolt.FinalCfBolt;
import framework.bolt.FinalMergeBolt;
import framework.bolt.GroupMergeBolt;
import framework.spout.CfSpout;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by rain on 15-11-25.
 */
public class CfByUser {
    public static void main(String[] args) throws Exception {
     //   int freq = Integer.parseInt(args[1]);   //发射速率
     //   float per = Float.parseFloat(args[2]);   //使用Bolt所占的比例(18*0.2，表明有18*0.2个bolt用来处理数据)
      //  String plan = args[3];   //方案：1， or 0
        String configFilePath = args[1];

        FileInputStream fis = new FileInputStream(configFilePath);
        // Properties类，主要用于读取以项目的配置文件（以．properties结尾的文件和xml文件）
        Properties configProps = new Properties();
        configProps.load(fis);

        Config conf = new Config();
        conf.setDebug(false);
        conf.setMaxSpoutPending(1000); //最大请求排队数(设置一个spout task上面最多有多少个没有处理的tuple)

        for (Object key : configProps.keySet()){
            String keySt = key.toString();
            String val = configProps.getProperty(keySt);
            conf.put(keySt, val);
    }

       String plan =String.valueOf(Integer.parseInt(configProps.getProperty("cfbyuser.plan")));
            //  Integer.parseInt(configProps.getProperty("cfbyuser.plan"));
        int freq =  Integer.parseInt(configProps.getProperty("cfbyuser.freq"));   //发射速率

        float per = Float.parseFloat(configProps.getProperty("cfbyuser.boltratio"));   //使用Bolt所占的比例(18*0.2，表明有18*0.2个bolt用来处理数据)
        int boltnum =  Integer.parseInt(configProps.getProperty("cfbyuser.group.boltnum"));   //发射速率
        int pro_num = (int) ( boltnum * per);  // 每个分组的处理个数，18是当前每个分组处理bolt数 (每个分组有18个bolt)

        String testfile =String.valueOf(configProps.getProperty("cfbyuser.testfile.dir"));  // 测试文件 /home/cf-data/test.txt
        String trainingfile =String.valueOf(configProps.getProperty("cfbyuser.trainingfile.dir")); //训练文件 /home/cf-data/test_training.txt

        TopologyBuilder builder = new TopologyBuilder();  //构建拓扑
        int spoutThreads = Integer.parseInt(configProps.getProperty("cfbyuser.cfspout.threads"));
        builder.setSpout("cf-spout1", new CfSpout(new File(testfile), 1, pro_num, freq),  spoutThreads );

        int FinalCfboltThreads = Integer.parseInt(configProps.getProperty("cfbyuser.FinalCfbolt.threads"));
        builder.setBolt("final-cf-bolt1", new FinalCfBolt(new File(trainingfile), plan),  FinalCfboltThreads)
                .allGrouping("cf-spout1");
        int GroupMergeboltThreads = Integer.parseInt(configProps.getProperty("cfbyuser.GroupMergebolt.threads"));
        builder.setBolt("group-merge-bolt1", new GroupMergeBolt(pro_num),GroupMergeboltThreads).shuffleGrouping("final-cf-bolt1");
        int FinalMergeboltThreads = Integer.parseInt(configProps.getProperty("cfbyuser.FinalMergebolt.threads"));
        builder.setBolt("final-merge-bolt", new FinalMergeBolt(), FinalMergeboltThreads)
                .shuffleGrouping("group-merge-bolt1");

        int numWorkers = Integer.parseInt(configProps.getProperty("num.workers"));

        conf.put(Config.TOPOLOGY_WORKERS,numWorkers);   //设置worker的个数

        ConfigExtension.setUseOldAssignment(conf, false);
        WorkerAssignment worker;
        List<WorkerAssignment> workers = new ArrayList<WorkerAssignment>();

        worker = new WorkerAssignment();
        worker.addComponent("cf-spout1" , 1);
        //worker.setHostName("172.17.0.2");  //要写上，不然可能没有运行结果
        worker.setCpu(1);
        worker.setMem(1024000000);
        workers.add(worker);


        worker = new WorkerAssignment();
        worker.addComponent("final-cf-bolt1" , 1);
      //  worker.setHostName("172.17.0.2");
        //worker.setPort(6701);
        worker.setCpu(1);
        worker.setMem(1024000000);
        workers.add(worker);

        worker = new WorkerAssignment();
        worker.addComponent("group-merge-bolt1", 1);
       // worker.setHostName("172.17.0.2");
      //  worker.setPort(6700);
        worker.setCpu(1);
        worker.setMem(1024000000);
        workers.add(worker);

        worker = new WorkerAssignment();
        worker.addComponent("final-merge-bolt", 1);  //1:Task个数
       // worker.setHostName("172.17.0.2");  //分配这个component到哪个机器
      //  worker.setPort(6700);
        worker.setCpu(1);   //为这个task分配的资源
        worker.setMem(1024000000);
        workers.add(worker);


        ConfigExtension.setUserDefineAssignment(conf, workers);
        if (args != null && args.length > 0) {
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();    //本地运行模式
            cluster.submitTopology("test", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        }
    }
}
