package com.adrianrafo.gcp4s.vision

import cats.data.EitherT
import cats.effect.Effect
import cats.syntax.either._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.adrianrafo.gcp4s.vision.ResponseHandler._
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

private[vision] object syntax {

  final class ImageAnnotatorClientOps[F[_]](client: ImageAnnotatorClient)(
      implicit E: Effect[F],
      EC: ExecutionContext) {

    def sendRequest(batchRequest: BatchAnnotateImagesRequest): EitherT[
      F,
      VisionError,
      BatchAnnotateImagesResponse] =
      ErrorHandlerService.asyncHandleError(
        client.batchAnnotateImagesCallable.futureCall(batchRequest).get(),
        visionErrorHandler)

  }

  final class BatchAnnotateImagesResponseOps(batchImageResponse: BatchAnnotateImagesResponse) {

    import scala.collection.JavaConverters._

    private def handleVisionResponse[A](
        handleResponse: AnnotateImageResponse => A): List[VisionResponse[A]] =
      batchImageResponse.getResponsesList.asScala
        .foldLeft(List.empty[VisionResponse[A]]) {
          case (list, res) => list :+ handleErrors(res, handleResponse)
        }

    def processLabels: VisionResponse[List[VisionLabel]] =
      handleErrors(batchImageResponse.getResponses(0), handleLabelResponse)

    def processLabelsPerImage: VisionBatchResponse[List[VisionLabel]] =
      handleVisionResponse(handleLabelResponse)

    def processText: VisionResponse[List[VisionText]] =
      handleErrors(batchImageResponse.getResponses(0), handleTextResponse)

    def processTextPerImage: VisionBatchResponse[List[VisionText]] =
      handleVisionResponse(handleTextResponse)

    def processDocumentText = ???
    def processDocumentTextPerImage = ???
    /*  // For full list of available annotations, see http://g.co/cloud/vision/docs
      TextAnnotation annotation = res.getFullTextAnnotation();
    for (Page page: annotation.getPagesList()) {
      String pageText = "";
      for (Block block : page.getBlocksList()) {
        String blockText = "";
        for (Paragraph para : block.getParagraphsList()) {
          String paraText = "";
          for (Word word: para.getWordsList()) {
            String wordText = "";
            for (Symbol symbol: word.getSymbolsList()) {
              wordText = wordText + symbol.getText();
              out.format("Symbol text: %s (confidence: %f)\n", symbol.getText(),
                symbol.getConfidence());
            }
            out.format("Word text: %s (confidence: %f)\n\n", wordText, word.getConfidence());
            paraText = String.format("%s %s", paraText, wordText);
          }
          // Output Example using Paragraph:
          out.println("\nParagraph: \n" + paraText);
          out.format("Paragraph Confidence: %f\n", para.getConfidence());
          blockText = blockText + paraText;
        }
        pageText = pageText + blockText;
      }
    }
    out.println("\nComplete annotation:");
    out.println(annotation.getText());
  }*/

    def processFace = ???
    def processFacePerImage = ???
    /*public static void detectFaces(String filePath, PrintStream out) throws Exception, IOException {

        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            out.printf("Error: %s\n", res.getError().getMessage());
            return;
          }

          // For full list of available annotations, see http://g.co/cloud/vision/docs
          for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
            out.printf(
              "anger: %s\njoy: %s\nsurprise: %s\nposition: %s",
              annotation.getAngerLikelihood(),
              annotation.getJoyLikelihood(),
              annotation.getSurpriseLikelihood(),
              annotation.getBoundingPoly());
          }
        }
      }
    }*/

    def processLogo = ???
    def processLogoPerImage = ???
    /*public static void detectLogos(String filePath, PrintStream out) throws Exception, IOException {

        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            out.printf("Error: %s\n", res.getError().getMessage());
            return;
          }

          // For full list of available annotations, see http://g.co/cloud/vision/docs
          for (EntityAnnotation annotation : res.getLogoAnnotationsList()) {
            out.println(annotation.getDescription());
          }
        }
      }
    }*/

    def processCropHints = ???
    def processCropHintsPerImage = ???
    /*
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            out.printf("Error: %s\n", res.getError().getMessage());
            return;
          }

          // For full list of available annotations, see http://g.co/cloud/vision/docs
          CropHintsAnnotation annotation = res.getCropHintsAnnotation();
          for (CropHint hint : annotation.getCropHintsList()) {
            out.println(hint.getBoundingPoly());
          }
        }
      }
    }*/

    def processLandmark = ???
    def processLandmarkPerImage = ???
    /*
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            out.printf("Error: %s\n", res.getError().getMessage());
            return;
          }

          // For full list of available annotations, see http://g.co/cloud/vision/docs
          for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
            LocationInfo info = annotation.getLocationsList().listIterator().next();
            out.printf("Landmark: %s\n %s\n", annotation.getDescription(), info.getLatLng());
          }
        }
      }
    }*/

    def processImageProperties = ???
    def processImagePropertiesPerImage = ???
    /*
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            out.printf("Error: %s\n", res.getError().getMessage());
            return;
          }

          // For full list of available annotations, see http://g.co/cloud/vision/docs
          DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors();
          for (ColorInfo color : colors.getColorsList()) {
            out.printf(
              "fraction: %f\nr: %f, g: %f, b: %f\n",
              color.getPixelFraction(),
              color.getColor().getRed(),
              color.getColor().getGreen(),
              color.getColor().getBlue());
          }
        }
      }
    }*/

    def processSafeSearch = ???
    def processSafeSearchPerImage = ???
    /*
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            out.printf("Error: %s\n", res.getError().getMessage());
            return;
          }

          // For full list of available annotations, see http://g.co/cloud/vision/docs
          SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
          out.printf(
            "adult: %s\nmedical: %s\nspoofed: %s\nviolence: %s\nracy: %s\n",
            annotation.getAdult(),
            annotation.getMedical(),
            annotation.getSpoof(),
            annotation.getViolence(),
            annotation.getRacy());
        }
      }
    }*/

    def processWebEntities = ???
    def processWebEntitiesPerImage = ???
    /*
        List<AnnotateImageResponse> responses = response.getResponsesList();

        for (AnnotateImageResponse res : responses) {
          if (res.hasError()) {
            out.printf("Error: %s\n", res.getError().getMessage());
            return;
          }

          // Search the web for usages of the image. You could use these signals later
          // for user input moderation or linking external references.
          // For a full list of available annotations, see http://g.co/cloud/vision/docs
          WebDetection annotation = res.getWebDetection();
          out.println("Entity:Id:Score");
          out.println("===============");
          for (WebEntity entity : annotation.getWebEntitiesList()) {
            out.println(entity.getDescription() + " : " + entity.getEntityId() + " : "
                + entity.getScore());
          }
          for (WebLabel label : annotation.getBestGuessLabelsList()) {
            out.format("\nBest guess label: %s", label.getLabel());
          }
          out.println("\nPages with matching images: Score\n==");
          for (WebPage page : annotation.getPagesWithMatchingImagesList()) {
            out.println(page.getUrl() + " : " + page.getScore());
          }
          out.println("\nPages with partially matching images: Score\n==");
          for (WebImage image : annotation.getPartialMatchingImagesList()) {
            out.println(image.getUrl() + " : " + image.getScore());
          }
          out.println("\nPages with fully matching images: Score\n==");
          for (WebImage image : annotation.getFullMatchingImagesList()) {
            out.println(image.getUrl() + " : " + image.getScore());
          }
          out.println("\nPages with visually similar images: Score\n==");
          for (WebImage image : annotation.getVisuallySimilarImagesList()) {
            out.println(image.getUrl() + " : " + image.getScore());
          }
        }
      }
    }*/

    def processObjectDetection = ???
    def processObjectDetectionPerImage = ???
    /*
        List<AnnotateImageResponse> responses = response.getResponsesList();

        // Display the results
        for (AnnotateImageResponse res : responses) {
          for (LocalizedObjectAnnotation entity : res.getLocalizedObjectAnnotationsList()) {
            out.format("Object name: %s\n", entity.getName());
            out.format("Confidence: %s\n", entity.getScore());
            out.format("Normalized Vertices:\n");
            entity
                .getBoundingPoly()
                .getNormalizedVerticesList()
                .forEach(vertex -> out.format("- (%s, %s)\n", vertex.getX(), vertex.getY()));
          }
        }
      }
    }*/

  }

}
