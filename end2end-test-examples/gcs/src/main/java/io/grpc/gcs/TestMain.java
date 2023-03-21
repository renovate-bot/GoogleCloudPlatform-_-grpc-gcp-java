package io.grpc.gcs;

import static io.grpc.gcs.Args.CLIENT_API_SERVICES_JSON;
import static io.grpc.gcs.Args.CLIENT_GCSIO_GRPC;
import static io.grpc.gcs.Args.CLIENT_GCSIO_GRPC_JAVA_STORAGE;
import static io.grpc.gcs.Args.CLIENT_GCSIO_JSON;
import static io.grpc.gcs.Args.CLIENT_GRPC;
import static io.grpc.gcs.Args.CLIENT_JAVA_GRPC;
import static io.grpc.gcs.Args.CLIENT_JAVA_JSON;

import com.google.cloud.hadoop.gcsio.GoogleCloudStorageFileSystemOptions.ClientType;
import java.io.FileInputStream;
import java.security.Security;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.conscrypt.Conscrypt;

public class TestMain {
  private static final Logger logger = Logger.getLogger(TestMain.class.getName());

  public static void main(String[] args) throws Exception {
    Args a = new Args(args);
    if (a.verboseLog) {
      LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
    }
    if (a.conscrypt) {
      Security.insertProviderAt(Conscrypt.newProvider(), 1);
    } else if (a.conscrypt_notm) {
      Security.insertProviderAt(Conscrypt.newProviderBuilder().provideTrustManager(false).build(),
          1);
    }
    ResultTable results = new ResultTable(a);
    switch (a.client) {
      case CLIENT_GRPC:
        GrpcClient grpcClient = new GrpcClient(a);
        results.start();
        grpcClient.startCalls(results);
        results.stop();
        break;
      case CLIENT_GCSIO_GRPC:
        // Json and old gRPC client use HTTP_API_CLIENT.
        GcsioClient gcsioGrpcClient = new GcsioClient(a, true, ClientType.HTTP_API_CLIENT);
        results.start();
        gcsioGrpcClient.startCalls(results);
        results.stop();
        break;
      case CLIENT_GCSIO_GRPC_JAVA_STORAGE:
        // New gRPC client uses STORAGE_CLIENT.
        GcsioClient gcsioGrpcJavaClient = new GcsioClient(a, true, ClientType.STORAGE_CLIENT);
        results.start();
        gcsioGrpcJavaClient.startCalls(results);
        results.stop();
        break;
      case CLIENT_GCSIO_JSON:
        // Json and old gRPC client use HTTP_API_CLIENT.
        GcsioClient gcsioJsonClient = new GcsioClient(a, false, ClientType.HTTP_API_CLIENT);
        results.start();
        gcsioJsonClient.startCalls(results);
        results.stop();
        break;
      case CLIENT_JAVA_GRPC:
      case CLIENT_JAVA_JSON:
        JavaClient javaClient = new JavaClient(a);
        results.start();
        javaClient.startCalls(results);
        results.stop();
        break;
      case CLIENT_API_SERVICES_JSON:
        JavaApiServicesClient javaApiServicesClient = new JavaApiServicesClient(a);
        results.start();
        javaApiServicesClient.startCalls(results);
        results.stop();
        break;
      default:
        logger.warning("Please provide --client");
    }
    results.printResult();
  }
}
