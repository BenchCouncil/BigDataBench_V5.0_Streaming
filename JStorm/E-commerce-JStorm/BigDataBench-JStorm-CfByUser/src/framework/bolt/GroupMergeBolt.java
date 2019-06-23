package framework.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wjw on 15-6-9.
 * modified by rain on 15-11-23
 */
public class GroupMergeBolt extends BaseRichBolt {
    OutputCollector collector;
    HashMap<String, Float[]> data;
    int resivenum;
    public GroupMergeBolt(int num) {
        this.resivenum = num;
    }
    @Override
    public void prepare(Map conf, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        data = new HashMap<>();
    }

    @Override
    public void execute(Tuple tuple) {
        String testinfo = tuple.getString(0);   //cfspout发出文件test.txt中的userid
        float avg = tuple.getFloat(1);   //
        float tmp1 = tuple.getFloat(2);  //
        float tmp2 = tuple.getFloat(3);   //
        long bolttime = tuple.getLong(4);  //
        long start = tuple.getLong(5);  //
        Float[] tmp;
        System.out.println("bolttime:" + testinfo + ";" + bolttime);

        if(data.get(testinfo) == null) {
            tmp = new Float[]{1.0f, tmp1, tmp2};
            data.put(testinfo, tmp);
        } else {
            tmp = data.get(testinfo);
            tmp[0] += 1;
            tmp[1] += tmp1;
            tmp[2] += tmp2;
            if (tmp[0] == resivenum) {
                data.remove(testinfo);
                collector.emit(new Values(testinfo, tmp[1], tmp[2], avg, start));
             //   System.out.println("totaltime:" + testinfo + "," + tmp[1] + ","+tmp[2]+","+avg+";" + start);
            } else {
                data.put(testinfo, tmp);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("testinfo", "tmp1", "tmp2", "avg", "begintime"));
    }
}
