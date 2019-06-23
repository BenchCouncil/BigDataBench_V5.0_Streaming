package framework.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import framework.operation.extrafun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Rick-Pc on 2015/7/29.
 */
public class IntactSearchBolt extends BaseRichBolt{
    OutputCollector collector;
 //   String hostname; //执行该程序的机器名字
  //  JedisPool pool;
    String plan; //是否执行特殊机制的标记变量
    String index_dir; //存放所有索引(18份)的路径
    String indexid_dir;
    long search_time; //要求的搜索时间
    Directory indexDir1;
    IndexReader indexreader1;
    IndexSearcher indexSearcher1;
    String[] fields = {"title", "content"};
    BooleanClause.Occur[] flags = new BooleanClause.Occur[] {
            BooleanClause.Occur.SHOULD, BooleanClause.Occur.MUST };

    public IntactSearchBolt(String index_dir,String indexid_dir,String plan,long search_time){
        this.index_dir = index_dir;  //"/home/search-data/index/data-"; 存放所有索引(18份)的路径
        this.indexid_dir = indexid_dir;   //indexid文件
        this.plan = plan;
        this.search_time = search_time;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    //    hostname = extrafun.gethostname();
           try {
                BufferedReader br = new BufferedReader(new FileReader(indexid_dir));  //读取indexid路径：indexid_dir = "/home/search-data/indexid"
                String index_id = br.readLine();   //按行读取
                br.close();
                //indexDir1:  /home/search-data/index/data-55/index
                indexDir1 = FSDirectory.open(new File(index_dir+index_id+"/index")); //读入本机器的索引
                indexreader1 = DirectoryReader.open(indexDir1);
                indexSearcher1 = new IndexSearcher(indexreader1);
             } catch (Exception e) {
                e.printStackTrace();
           }

    }

    @Override
    public void execute(Tuple input) {   //Tuple："search_word", "begintime"
        String word = input.getString(0); //搜索词
        long start_time = input.getLong(1); //搜索词的发射时刻
      //  ArrayList<String> hosts = (ArrayList<String>) input.getValue(2);
        String urls_score = "";
        int search_num = 0;
                try {
                    Query query = MultiFieldQueryParser.parse(word, fields, flags, new SmartChineseAnalyzer());
                    TopDocs docs = indexSearcher1.search(query,10000);
                    ScoreDoc[] scoreDocs = docs.scoreDocs;
                    int numhits = scoreDocs.length;
                    //for test：只取了搜索结果的top10
                    int length = 10;
                    if(length>scoreDocs.length)
                        length = scoreDocs.length;
                    ScoreDoc[] subDocs = new ScoreDoc[length];
                    for(int i=0;i<length;i++)
                        subDocs[i]=scoreDocs[i];
                    urls_score = Arrays.asList(subDocs).toString();
                   // urls_score = hostname+"+"+numhits+"+"+urls_score;
                    urls_score = numhits+"+"+urls_score;
                } catch (Exception e) {
                    e.printStackTrace();
                }

        long bolt_time = System.currentTimeMillis() - start_time;
        //collector.emit(new Values(word,hostname,bolt_time,urls_score,start_time,search_num));
        collector.emit(new Values(word,bolt_time,urls_score,start_time,search_num));
        System.out.println(word+","+bolt_time+","+urls_score+","+start_time+","+search_num);
        collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
       // outputFieldsDeclarer.declare(new Fields("word","hostname","bolt_time","urls_score","start_time","search_num"));
        outputFieldsDeclarer.declare(new Fields("word","bolt_time","urls_score","start_time","search_num"));
    }
}
