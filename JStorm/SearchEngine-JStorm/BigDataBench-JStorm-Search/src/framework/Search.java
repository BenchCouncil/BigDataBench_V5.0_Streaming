package framework;

/**
 * Created by ACS on 2015/11/28.
 */
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import com.alibaba.jstorm.client.ConfigExtension;
import com.alibaba.jstorm.client.WorkerAssignment;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import framework.spout.SearchSpout;
import framework.bolt.IntactSearchBolt;
import framework.bolt.IntactMergeBolt;
import framework.bolt.SearchMergeBolt;

/**
 *
 */
public class Search {
    public static void main(String[] args) throws Exception{
        String configFilePath = args[1];

        FileInputStream fis = new FileInputStream(configFilePath);
        // Properties类，主要用于读取以项目的配置文件（以．properties结尾的文件和xml文件）
        Properties configProps = new Properties();
        configProps.load(fis);

        Config conf = new Config();
        conf.setDebug(false);
        conf.setMaxSpoutPending(1000); //发射之后等待的元组数
        for (Object key : configProps.keySet()){
            String keySt = key.toString();
            String val = configProps.getProperty(keySt);
            conf.put(keySt, val);
        }

        String plan =String.valueOf(Integer.parseInt(configProps.getProperty("search.plan")));//1:使用特殊机制 0:不使用特殊机制
        int freq =  Integer.parseInt(configProps.getProperty("search.freq"));   //发射速率
        float per = Float.parseFloat(configProps.getProperty("search.boltratio"));   //使用Bolt所占的比例(18*0.2，表明有18*0.2个bolt用来处理数据)
        int boltnum =  Integer.parseInt(configProps.getProperty("search.group.boltnum"));
        int pro_num = (int) (boltnum * per);

        String wordsdir =String.valueOf(configProps.getProperty("searchwords.dir"));  //  "/home/search-data/searchwords.txt"; //搜索词的文件路径
        String indexid_dir =String.valueOf(configProps.getProperty("indexid.dir")); // "/home/search-data/indexid"; //存放索引编号的文件
        String index_dir = String.valueOf(configProps.getProperty("index.dir")); //"/home/search-data/index/data-"; //存放所有索引(18份)的路径
        long search_time = 0l;

        TopologyBuilder builder = new TopologyBuilder();
        int spoutThreads = Integer.parseInt(configProps.getProperty("search.searchSpout.threads"));
        builder.setSpout("search-spout1",new SearchSpout(new File(wordsdir),1,pro_num,freq),spoutThreads); //搜索词发射组件
        int IntactSearchBoltThreads = Integer.parseInt(configProps.getProperty("search.IntactSearchBolt.threads"));
        builder.setBolt("intact-search-bolt1",new IntactSearchBolt(index_dir,indexid_dir,plan,search_time),IntactSearchBoltThreads).allGrouping("search-spout1"); //完整索引搜索组件
        int IntactMergeBoltThreads = Integer.parseInt(configProps.getProperty("search.IntactMergeBolt.threads"));
        builder.setBolt("intact-merge-bolt1",new IntactMergeBolt(pro_num), IntactMergeBoltThreads).shuffleGrouping("intact-search-bolt1"); //完整索引整合组件
        int FinalMergeBoltThreads = Integer.parseInt(configProps.getProperty("search.FinalMergeBolt.threads"));
        builder.setBolt("final-merge-bolt",new SearchMergeBolt(),FinalMergeBoltThreads)
                .shuffleGrouping("intact-merge-bolt1");

        int numWorkers = Integer.parseInt(configProps.getProperty("num.workers"));
            conf.put(Config.TOPOLOGY_WORKERS,numWorkers);

        ConfigExtension.setUseOldAssignment(conf,false);
     //   String[][] hostnames = Machines.HOSTNAMES; //集群中所有的机器
        WorkerAssignment worker; //以下进行Worker的分配
        List<WorkerAssignment> workers = new ArrayList<WorkerAssignment>();

            worker = new WorkerAssignment();
            worker.addComponent("search-spout1" , 1); //配置6个搜索词发射组件，每个组件1个task
        //    worker.setHostName(hostnames[0][i]);
            //  worker.setPort(6700);
            worker.setCpu(1);
            worker.setMem(1024000000);
            workers.add(worker);

            worker = new WorkerAssignment();
            worker.addComponent("intact-merge-bolt1" , 1); //配置6个完整索引整合组件，每个组件1个task
           // worker.setHostName(hostnames[0][i + 12]);
            // worker.setPort(6700);
            worker.setCpu(1);
            worker.setMem(1024000000);
            workers.add(worker);

                worker = new WorkerAssignment();
                worker.addComponent("intact-search-bolt1" , 1); //配置18*6个完整索引搜索组件，每个组件1个task
             //   worker.setHostName(hostnames[i][j]);
                //   worker.setPort(6701);
                worker.setCpu(1);
                worker.setMem(1024000000);
                workers.add(worker);

          worker = new WorkerAssignment();
          worker.addComponent("final-merge-bolt",1); //在worker里放一个final-merge-bolt组件的task
        //  worker.setHostName(hostnames[0][0]);
        //worker.setPort(6700);
          worker.setCpu(1);
          worker.setMem(1024000000);
          workers.add(worker);

        ConfigExtension.setUserDefineAssignment(conf, workers);

        if (args != null && args.length > 0) {
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        }
    }
}

