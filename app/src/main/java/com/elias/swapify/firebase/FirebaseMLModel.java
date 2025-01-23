package com.elias.swapify.firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import java.io.FileOutputStream;
import java.io.IOException;
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
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel(modelName, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                            interpreter = new Interpreter(modelFile);
                            onModelReady();
                        }
                    }
                });
    }

    private void onModelReady() {
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    private Bitmap tensorImageToBitmap(TensorImage tensorImage) {
        ByteBuffer buffer = tensorImage.getBuffer();
        buffer.rewind();

        Bitmap bitmap = Bitmap.createBitmap(IMG_SIZE, IMG_SIZE, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[IMG_SIZE * IMG_SIZE];
        for (int i = 0; i < pixels.length; i++) {
            int r = buffer.get() & 0xFF;
            int g = buffer.get() & 0xFF;
            int b = buffer.get() & 0xFF;
            pixels[i] = Color.rgb(r, g, b);
        }
        bitmap.setPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE);
        return bitmap;
    }

    public String predictImageCategory(Bitmap image, Context context) {
        Bitmap resizedImage = Bitmap.createScaledBitmap(image, IMG_SIZE, IMG_SIZE, true);

        TensorImage tensorImage = new TensorImage(DataType.UINT8);
        tensorImage.load(resizedImage);
        tensorImage = new ImageProcessor.Builder()
                .add(new ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(quantizeOp)
                .build()
                .process(tensorImage);

        Bitmap processedBitmap = tensorImageToBitmap(tensorImage);
        saveProcessedImage(context, processedBitmap);

        ByteBuffer inputBuffer = tensorImage.getBuffer();
        int[] outputShape = interpreter.getOutputTensor(0).shape();
        DataType outputDataType = interpreter.getOutputTensor(0).dataType();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

        interpreter.run(inputBuffer, outputBuffer.getBuffer().rewind());

        float[] probabilities = outputBuffer.getFloatArray();
        Log.d("FirebaseMLModel", "Length of probabilities array: " + probabilities.length);

        int predictedIndex = getMaxProbabilityIndex(probabilities);
        if (predictedIndex >= probabilities.length) {
            Log.e("FirebaseMLModel", "Predicted index is out of bounds: " + predictedIndex);
            return "Error: Predicted index out of bounds";
        }

        String predictedCategory = indexToCategory(predictedIndex);
        return predictedCategory;
    }

    private void saveProcessedImage(Context context, Bitmap bitmap) {
        File dir = new File(context.getExternalFilesDir(null), "processed_images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "processed_image.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.d("FirebaseMLModel", "Processed image saved at: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("FirebaseMLModel", "Error saving processed image", e);
        }
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