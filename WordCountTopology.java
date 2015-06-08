import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

/**
 * This is a topology that does simple word counts
 */
public final class WordCountTopology {
  private WordCountTopology() {
  }

  /**
   * A spout that emits a random word
   */
  static class WordSpout extends BaseRichSpout {
    private Random rnd;
    private SpoutOutputCollector collector;
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
      outputFieldsDeclarer.declare(new Fields("word"));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext,
                     SpoutOutputCollector spoutOutputCollector) {
      rnd = new Random(31);
      collector = spoutOutputCollector;
    }

    @Override
    public void nextTuple() {
      String[] list = {"Jack", "Mary", "Jill", "McDonald"};
      Utils.sleep(1000);
      int nextInt = rnd.nextInt(list.length);
      collector.emit(new Values(list[nextInt]));
    }
  }

  /**
   * A bolt that counts the words that it receives
   */
  static class ConsumerBolt extends BaseRichBolt {
    private OutputCollector collector;
    private Map<String, Integer> countMap;
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
      collector = outputCollector;
      countMap = new HashMap<String, Integer>();
    }

    @Override
    public void execute(Tuple tuple) {
      String key = tuple.getString(0);
      if (countMap.get(key) == null) {
        countMap.put(key, 1);
      } else {
        Integer val = countMap.get(key);
        countMap.put(key, ++val);
      }
      collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
  }

  /**
   * Main method
   */
  public static void main(String[] args) {
    TopologyBuilder builder = new TopologyBuilder();
    builder.setSpout("word", new WordSpout(), 2);
    builder.setBolt("consumer", new ConsumerBolt(), 2)
        .fieldsGrouping("word", new Fields("word"));
    Config conf = new Config();
    /*
    Set config here
    */

    try {
      StormSubmitter.submitTopology("topology", conf, builder.createTopology());
    } catch (AlreadyAliveException e) {
      e.printStackTrace();
    } catch (InvalidTopologyException e) {
      e.printStackTrace();
    }
  }
}