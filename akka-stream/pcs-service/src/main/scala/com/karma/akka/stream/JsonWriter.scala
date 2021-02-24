package com.karma.akka.stream

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.ConnectionFactory
import com.google.gson.Gson
import com.netapp.asup.common.event.AsupExtractedEvent
import com.netapp.asup.perf.counter.core.PerfCounterCore
import org.apache.hadoop.hbase.client.Connection


object JsonWriter {
 
  def write(asupExtractedEventJson: String, connection:Connection): Unit = {
    val gson = new Gson
    val asupExtractedEvent = gson.fromJson(asupExtractedEventJson, classOf[AsupExtractedEvent])
    println(asupExtractedEvent)
//    val conf = HBaseConfiguration.create();
//    val ZOOKEEPER_QUORUM = "rtpwil-bigdata-qa17.rtp.openenglab.netapp.com,rtpwil-bigdata-qa18.rtp.openenglab.netapp.com,rtpwil-bigdata-qa19.rtp.openenglab.netapp.com"
//    val PORT = "2181"
//    conf.set("hbase.zookeeper.property.clientPort", PORT);
//    conf.set("hbase.zookeeper.quorum", ZOOKEEPER_QUORUM);
//    var hbaseConnection = ConnectionFactory.createConnection(conf);
    val schemaPath = "/Users/singg/openws/perf-counter-legacy-core/schema";
    PerfCounterCore.processEvent(asupExtractedEvent, connection, schemaPath, 100);
    //		try {
    //			hbaseConnection = ConnectionFactory.createConnection(conf);
    //			PerfCounterCore.processEvent(asupExtractedEvent, hbaseConnection, schemaPath, 100);
    //		} catch (IOException e) {
    //			e.printStackTrace();
    //		} catch (ForkedRecordWriterException e) {
    //			e.printStackTrace();
    //		} catch (SectionNotFoundException e) {
    //			e.printStackTrace();
    //		} catch (UnsupportedAsupTypeException e) {
    //			e.printStackTrace();
    //		} finally {
    //			try {
    //				if (hbaseConnection != null)
    //					hbaseConnection.close();
    //			} catch (IOException e) {
    //				e.printStackTrace();
    //			}
    //		}
    //    PerfCounterCore.processEvent(asupExtractedEvent, )
  }
}