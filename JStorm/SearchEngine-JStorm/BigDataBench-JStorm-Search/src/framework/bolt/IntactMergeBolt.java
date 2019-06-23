package framework.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class IntactMergeBolt extends BaseRichBolt {
    OutputCollector collector;
    int bolt_num; //参与搜索的bolt数目
    HashMap<String,String> result;
    HashMap<String,Integer> cal_num;
    public IntactMergeBolt(int num){
        this.bolt_num = num;
    }
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        result = new HashMap<String, String>();
        cal_num = new HashMap<String, Integer>();
    }

    @Override
    public void execute(Tuple input) { //Tuple:"word","bolt_time","urls_score","start_time","search_num"
        String word = input.getString(0); //搜索词
     //   String hostname = input.getString(1);
     //   long bolt_time = input.getLong(2);
     //   String urls_score = input.getString(3);
     //   long start_time = input.getLong(4);
     //   int search_num = input.getInteger(5);
        long bolt_time = input.getLong(1);
        String urls_score = input.getString(2);
        long start_time = input.getLong(3);
        int search_num = input.getInteger(4);
        String urls = "";
        if(result.get(word) == null){
            result.put(word,urls_score);
            cal_num.put(word,1);
        }else{
            if(!urls_score.equals(""))
                urls = result.get(word)+";"+urls_score;
            int cal = cal_num.get(word)+1;
            if(cal==bolt_num) {
                collector.emit(new Values(word,urls,start_time));
                 System.out.println("search_word:"+word+",url:"+urls+",starttime"+start_time);
                result.remove(word);
                cal_num.remove(word);
            }else{
                cal_num.put(word,cal);
                result.put(word,urls);
            }
        }
        collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("word","urls","start_time"));
    }
}
