import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.Face;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.ListFacesRequest;
import com.amazonaws.services.rekognition.model.ListFacesResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IndexAndListFacesExample {
   public static final String COLLECTION_ID = "nnaircollection";
   public static final String S3_BUCKET = "mynnairnvirginia";

   public static void main(String[] args) throws Exception {
      AWSCredentials credentials;
      try {
         credentials = new ProfileCredentialsProvider("default").getCredentials();
      } catch (Exception e) {
         throw new AmazonClientException(
             "Cannot load the credentials from the credential profiles file. " +
                 "Please make sure that your credentials file is at the correct " +
                 "location (/Users/userid/.aws/credentials), and is in valid format.",
             e);
      }

      ObjectMapper objectMapper = new ObjectMapper();


      AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder
          .standard()
          .withRegion(Regions.US_EAST_1)
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .build();


      // 1. Index face 1
      Image image = getImageUtil(S3_BUCKET, "saisample.jpg");
      String externalImageId = "saisample.jpg";
      IndexFacesResult indexFacesResult = callIndexFaces(COLLECTION_ID,
          externalImageId, "ALL", image, amazonRekognition);
      System.out.println(externalImageId + " added");
      List < FaceRecord > faceRecords = indexFacesResult.getFaceRecords();
      for (FaceRecord faceRecord: faceRecords) {
         System.out.println("Face detected: Faceid is " +
             faceRecord.getFace().getFaceId());
      }

      //// 2. Index face 2
      //indexFacesResult = null;
      //faceRecords = null;
      //Image image2 = getImageUtil(S3_BUCKET, "nidhish2.jpg");
      //String externalImageId2 = "image2.jpg";
      //System.out.println(externalImageId2 + " added");
      //indexFacesResult = callIndexFaces(COLLECTION_ID, externalImageId2,
      //    "ALL", image2, amazonRekognition);
      //faceRecords = indexFacesResult.getFaceRecords();
      //for (FaceRecord faceRecord: faceRecords) {
      //   System.out.println("Face detected. Faceid is " +
      //       faceRecord.getFace().getFaceId());
      //}


      // 3. Page through the faces with ListFaces
      ListFacesResult listFacesResult = null;
      System.out.println("Faces in collection " + COLLECTION_ID);

      String paginationToken = null;
      do {
         if (listFacesResult != null) {
            paginationToken = listFacesResult.getNextToken();
         }
         listFacesResult = callListFaces(COLLECTION_ID, 1, paginationToken,
             amazonRekognition);
         List < Face > faces = listFacesResult.getFaces();
         for (Face face: faces) {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(face));
         }
      } while (listFacesResult != null && listFacesResult.getNextToken() !=
          null);
   }

   private static IndexFacesResult callIndexFaces(String collectionId, String externalImageId,
       String attributes, Image image, AmazonRekognition amazonRekognition) {
      IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
          .withImage(image)
          .withCollectionId(collectionId)
          .withExternalImageId(externalImageId)
          .withDetectionAttributes(attributes);
      return amazonRekognition.indexFaces(indexFacesRequest);

   }

   private static ListFacesResult callListFaces(String collectionId, int limit,
       String paginationToken, AmazonRekognition amazonRekognition) {
      ListFacesRequest listFacesRequest = new ListFacesRequest()
          .withCollectionId(collectionId)
          .withMaxResults(limit)
          .withNextToken(paginationToken);
      return amazonRekognition.listFaces(listFacesRequest);
   }

   private static Image getImageUtil(String bucket, String key) {
      return new Image()
          .withS3Object(new S3Object()
              .withBucket(bucket)
              .withName(key));
   }

}
