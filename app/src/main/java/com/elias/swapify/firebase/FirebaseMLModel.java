package com.elias.swapify.firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class FirebaseMLModel {
    private Interpreter interpreter;
    private static final int IMG_SIZE = 64;
    // Update these constants with the actual mean and std values used for training
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;

    // These constants are used for quantization and dequantization
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;

    // Add any other preprocessing steps you need for your model
    private TensorOperator quantizeOp = new QuantizeOp(IMAGE_MEAN, IMAGE_STD);
    private TensorOperator dequantizeOp = new DequantizeOp(PROBABILITY_MEAN, PROBABILITY_STD);

    public void downloadAndInitializeModel(Context context, String modelName) {
        // Define download conditions for the model
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi() // This condition can be changed based on requirements
                .build();

        // Start model download
        FirebaseModelDownloader.getInstance()
                .getModel(modelName, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        // Model download complete
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                            // Initialize the TensorFlow Lite interpreter with the downloaded model
                            interpreter = new Interpreter(modelFile);
                            // Notify the rest of your application that the model is ready for use
                            // This could be a callback, a LiveData update, or a similar mechanism
                            onModelReady();
                        }
                    }
                });
    }

    private void onModelReady() {
        // Implementation depends on how you want to notify the activities or fragments
        // For example, you could use a callback interface or LiveData to notify the UI components
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public String predictImageCategory(Bitmap image) {
        // Resize the image and convert it to a ByteBuffer
        Bitmap resizedImage = Bitmap.createScaledBitmap(image, IMG_SIZE, IMG_SIZE, true);

        // Create a TensorImage object from the resized bitmap
        TensorImage tensorImage = new TensorImage(DataType.UINT8);
        tensorImage.load(resizedImage);
        tensorImage = new ImageProcessor.Builder()
                .add(new ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(quantizeOp) // Quantize the image to UINT8
                .build()
                .process(tensorImage);

        // Run inference using the interpreter
        ByteBuffer inputBuffer = tensorImage.getBuffer();
        int[] outputShape = interpreter.getOutputTensor(0).shape();
        DataType outputDataType = interpreter.getOutputTensor(0).dataType();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);
        // Run the model
        interpreter.run(inputBuffer, outputBuffer.getBuffer().rewind());

        // Get the prediction and handle potential out-of-bounds error
        float[] probabilities = outputBuffer.getFloatArray();
        Log.d("FirebaseMLModel", "Length of probabilities array: " + probabilities.length);

        int predictedIndex = getMaxProbabilityIndex(probabilities);
        if (predictedIndex >= probabilities.length) {
            Log.e("FirebaseMLModel", "Predicted index is out of bounds: " + predictedIndex);
            return "Error: Predicted index out of bounds";
        }

        // Assuming you have a method to map predictedIndex to category name
        String predictedCategory = indexToCategory(predictedIndex);
        return predictedCategory;
    }

    private int getMaxProbabilityIndex(float[] probabilities) {
        int maxIndex = 0;
        float maxProbability = 0.0f;
        for (int i = 0; i < probabilities.length; i++) {
            Log.d("FirebaseMLModel", "Category " + i + ": " + probabilities[i]);
            if (probabilities[i] > maxProbability) {
                maxProbability = probabilities[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private String indexToCategory(int index) {
        // Implement this based on your categories
        // if index == 0, return "books";
        // if index == 1, return "funkoPopsAndFigures";
        // if index == 2, return "retroKits";
        // if index == 3, return "shoes";
        // if index == 4, return "tradingCards";

        // You can use a String array or a HashMap for mapping indexes to category names
        HashMap<Integer, String> categoryMap = new HashMap<>();
        categoryMap.put(0, "Books");
        categoryMap.put(1, "Funko Pops and Figures");
        categoryMap.put(2, "Retro Kits");
        categoryMap.put(3, "Shoes");
        categoryMap.put(4, "Trading Cards");

        return categoryMap.get(index);
    }
}