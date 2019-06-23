package framework.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import framework.operation.extrafun;
import framework.util.Machines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;


public class SearchSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;
    ArrayList<String> data = new ArrayList<String>(); //存储搜索词的List
    int i = 0;
    int freq; //搜索词发送速率
    File wordsfile;  //文件
    int flag; //空跑时间标记变量
    int index;
    int pro_num;
    int word_num; //搜索词的数量
    public SearchSpout(File wordsfile, int index, int pro_num, int freq) { //保存搜索词的文件，文件的index（1-6），搜索组件的个数，搜索词的发送频率
        this.wordsfile = wordsfile;
        this.index = index;
        this.pro_num = pro_num;
        this.freq = freq;
    }
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
        BufferedReader reader;
        String str;
        try {
            reader = new BufferedReader(new FileReader(wordsfile)); //读取搜索词文件:/home/search-data/searchwords.txt
            while ((str = reader.readLine()) != null)
                data.add(str);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        word_num = data.size(); //搜索词的数量
    }

    @Override
    public void nextTuple() {
        if(flag > 30) {//使用框架时开始前空跑了3分钟；不使用框架时可改为30s
        //    ArrayList<String> hosts = extrafun.hostslist(Machines.HOSTNAMES[index], pro_num);   //随机选取部分机器
            long start_time = System.currentTimeMillis();
            int j;
            if (i <=word_num) { //发射搜索词数
                j = i % word_num;
                System.out.println(i+data.get(j));
               // _collector.emit(new Values(data.get(j), start_time, hosts));
                _collector.emit(new Values(data.get(j), start_time));
                i++;
            }
            Utils.sleep((long) (1000 / freq));
        } else {
            flag ++;
            System.out.println("waiting:"+flag);
            Utils.sleep(1000);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //declarer.declare(new Fields("search_word", "begintime", "hosts"));
        declarer.declare(new Fields("search_word", "begintime"));
    }
}
