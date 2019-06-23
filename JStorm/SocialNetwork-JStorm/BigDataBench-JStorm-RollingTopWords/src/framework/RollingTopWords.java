package framework;

/**
 * Created by ACS on 2015/11/25.
 */
import backtype.storm.LocalCluster;
import framework.bolt.RankBolt;
import framework.bolt.MergeObjectsBolt;
import framework.bolt.RollingCountObjects;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.alibaba.jstorm.client.ConfigExtension;
import com.alibaba.jstorm.client.WorkerAssignment;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RollingTopWords {
    public static void main(String[] args) throws Exception {
   //  final int TOP_N=3 ;
        String spoutId = "wordGenerator";
        String counterId = "counter";
        String RankerId = "Ranker";
        String MergeId = "Merger";
        String configFilePath = args[1];

        Config conf = new Config();
        conf.setDebug(true);
      //  conf.setNumWorkers(20);
        conf.setMaxSpoutPending(5000);

        FileInputStream fis = new FileInputStream(configFilePath);
        // Properties类，主要用于读取以项目的配置文件（以．properties结尾的文件和xml文件）
        Properties configProps = new Properties();
        configProps.load(fis);

        for (Object key : configProps.keySet()){
            String keySt = key.toString();
            String val = configProps.getProperty(keySt);
            conf.put(keySt, val);
        }
        final int TOP_N=Integer.parseInt(configProps.getProperty("rollingTopWords.topN")); ;

        TopologyBuilder builder = new TopologyBuilder();
        int spoutThreads = Integer.parseInt(configProps.getProperty("rollingTopWords.spout.threads"));
        builder.setSpout(spoutId, new TestWordSpout(),spoutThreads);  // TestWordSpout()

        int RollingCountboltThreads = Integer.parseInt(configProps.getProperty("rollingTopWords.RollingCountbolt.threads"));
        builder.setBolt(counterId, new RollingCountObjects(60, 10), RollingCountboltThreads).fieldsGrouping(
                spoutId, new Fields("word"));   //RollingCountObjects
        int RankboltThreads = Integer.parseInt(configProps.getProperty("rollingTopWords.Rankbolt.threads"));
        builder.setBolt(RankerId, new RankBolt(TOP_N), RankboltThreads).fieldsGrouping(counterId,
                new Fields("obj"));   //RankBolt(TOP_N)
        builder.setBolt(MergeId, new MergeObjectsBolt(TOP_N)).globalGrouping(RankerId);  // MergeObjectsBolt(TOP_N)

        int numWorkers = Integer.parseInt(configProps.getProperty("num.workers"));
        conf.put(Config.TOPOLOGY_WORKERS,numWorkers);   //设置worker的个数

        ConfigExtension.setUseOldAssignment(conf, false);
        WorkerAssignment worker;
        List<WorkerAssignment> workers = new ArrayList<WorkerAssignment>();

        worker = new WorkerAssignment();
        worker.addComponent(spoutId , 1);
        //worker.setHostName("172.17.0.2");
        worker.setCpu(1);
        worker.setMem(1024000000);
        workers.add(worker);


        worker = new WorkerAssignment();
        worker.addComponent(counterId , 1);
        //  worker.setHostName("172.17.0.2");
        //worker.setPort(6701);
        worker.setCpu(1);
        worker.setMem(1024000000);
        workers.add(worker);

        worker = new WorkerAssignment();
        worker.addComponent(RankerId , 1);
        //worker.setHostName("172.17.0.2");
        worker.setCpu(1);
        worker.setMem(1024000000);
        workers.add(worker);


        worker = new WorkerAssignment();
        worker.addComponent(MergeId , 1);
        //  worker.setHostName("172.17.0.2");
        //worker.setPort(6701);
        worker.setCpu(1);
        worker.setMem(1024000000);
        workers.add(worker);

        ConfigExtension.setUserDefineAssignment(conf, workers);
        if (args != null && args.length > 0) {
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
            Thread.sleep(10000);
        } else {
            LocalCluster cluster = new LocalCluster();    //本地运行模式
            cluster.submitTopology("demo", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        }
    }
}