package framework.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;


public class SearchMergeBolt extends BaseRichBolt {
    OutputCollector collector;
    HashMap<String,String> result;
    HashMap<String,Integer> cal_num;
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        result = new HashMap<String, String>();
        cal_num = new HashMap<String, Integer>();
    }

    @Override
    public void execute(Tuple input) {   //Tuple:"word","urls","start_time"
        String word = input.getString(0); //搜索词
        String urls = input.getString(1);
        long start_time = input.getLong(2);
        String urls_score = "";
        if(result.get(word) == null){
            result.put(word,urls);
            cal_num.put(word,1);
        }else{
            if(!urls.equals(""))
                urls_score = result.get(word)+";"+urls;
            int cal = cal_num.get(word)+1;
            if(cal==6) {
                long totaltime = System.currentTimeMillis()-start_time;
                //System.out.println(word+":"+"totaltime:"+totaltime+":result:"+urls_score);
                System.out.println(word+":"+totaltime+":"+urls_score);
                result.remove(word);
                cal_num.remove(word);
            } else {
                cal_num.put(word,cal);
                result.put(word,urls_score);
            }

        }
        collector.ack(input);

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
