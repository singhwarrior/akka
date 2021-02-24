package com.karma.akka.stream.db.constants

object TABLE_NAME {
  val SERVICE_REGISTRY = "SERVICE_REGISTRY"
  //  {
  //    "_id" : ObjectId("5abc969c24b5ecd646e9b427"),
  //    "service_name" : "SES",
  //    "monitor_topics" : [
  //        {
  //            "monitor_topic" : "asup_routing_cruise_lane_topic",
  //            "predecessor_topic" : "asup_extrcated_cruise_lane_topic"
  //        },
  //        {
  //            "monitor_topic" : "asup_routing_car_lane_topic",
  //            "predecessor_topic" : "asup_extrcated_car_lane_topic"
  //        },
  //        {
  //            "monitor_topic" : "asup_routing_minitruck_lane_topic",
  //            "predecessor_topic" : "asup_extrcated_minitruck_lane_topic"
  //        },
  //        {
  //            "monitor_topic" : "asup_routing_truck_lane_topic",
  //            "predecessor_topic" : "asup_extrcated_truck_lane_topic"
  //        }
  //    ],
  //    "failure_info" : {
  //        "failed_topic" : "ses_failed_topic",
  //        "requeue_topic_map" : {
  //            "CRUISE_LANE" : "ses_cruise_lane_requeue_topic",
  //            "CAR_LANE" : "ses_car_lane_requeue_topic",
  //            "MINITRUCK_LANE" : "ses_minitruck_lane_requeue_topic",
  //            "TRUCK_LANE" : "ses_truck_lane_requeue_topic"
  //        },
  //        "max_requeue_count" : 2.0
  //    }
  //}
  val FAILED_EVENT = "FAILED_EVENT"
  //  {
  //    "_id" : ObjectId("5a9543e4dd415b69a6e69251"),
  //    "asup_id" : "2018022620130118",
  //    "service_name" : "SES",
  //    "q_count" : 2,
  //    "event" : "{\"asupId\":\"2018022620130118\",\"asupType\":\"DOT-MGMTLOG\",\"asupSize\":\"0.11993789672851562 MB\",\"rawAsupPath\":\"/asupmail/spool/asups/post/asupdw/2018022620130118.headers\",\"asupProcessingLane\":\"CAR_LANE\",\"producedBy\":\"routing-service\",\"asupReceivedTimestamp\":1519731671198,\"producedTimestamp\":1519731672962,\"receivedTimestamp\":0,\"completedTimestamp\":1519731672962,\"addOnAttributes\":{\"secureFlag\":\"false\"},\"status\":\"SUCCEED\"}",
  //    "failureCategory" : "PROCESSING_ERROR",
  //    "failureSubCategory" : "FILE_IO_ERROR",
  //    "failureType" : "DATA_PARSING",
  //    "failureException" : "net.sf.sevenzipjbinding.SevenZipException",
  //    "failureDescription" : "Archive file can't be opened with none of the registered codecs",
  //    "timestamp" : 1519731701065.0
  //}
}