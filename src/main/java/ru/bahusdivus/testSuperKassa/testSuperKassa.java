package ru.bahusdivus.testSuperKassa;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class testSuperKassa {
    private String inputFileName;
    private String outputFileName;
    private ClassLoader classloader;
    private int routeLength;
    private byte maskLength;

    public static void main(String[] args) {
        testSuperKassa tsk = new testSuperKassa("input.json", "output.json");
        tsk.makeCompliteRoutes();
    }

    private testSuperKassa(String inputFileName, String outputFileName) {
        this.classloader = Thread.currentThread().getContextClassLoader();
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
    }

    private JSONArray load(String fileName) {
        StringBuilder input = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream(fileName)), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                input.append(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        JSONArray jsonInput = new JSONArray(input.toString());
        routeLength = ((JSONArray) jsonInput.get(0)).length();
        maskLength = (byte) (Math.pow(2, routeLength) - 1);
        return jsonInput;
    }

    private void makeCompliteRoutes() {

        JSONArray jsonInput = load(inputFileName);

        OneEntry[] map = jsonToEntryArray(jsonInput);

        ArrayList<ArrayList<String[]>> outputList = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            ArrayList<String[]> list = new ArrayList<>();
            list.add(map[i].getValues());
            findFull(map, i + 1, list, map[i].getMask(), outputList);
        }

        JSONArray outputJson = new JSONArray();
        for (ArrayList<String[]> collect : outputList) {
            String[] rowArray = new String[routeLength];
            for (int i = 0; i < routeLength; i++) {
                for (String[] strings : collect) {
                    if (strings[i] != null) rowArray[i] = strings[i];
                }
            }
            outputJson.put(rowArray);
        }
        save(outputFileName, outputJson);
    }

    private void save(String fileName, JSONArray outputJson) {
        try (OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(Objects.requireNonNull(classloader.getResource(fileName)).getPath()), StandardCharsets.UTF_8)){
            outputStream.write(outputJson.toString());
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private OneEntry[] jsonToEntryArray(JSONArray jsonInput) {
        OneEntry[] map = new OneEntry[jsonInput.length()];
        for (int i = 0; i < jsonInput.length(); i++) {
            JSONArray innerArray = (JSONArray) jsonInput.get(i);
            String[] values = new String[routeLength];
            byte mask = 0;
            for (int j = 0; j < routeLength; j++) {
                if (!innerArray.get(j).equals(JSONObject.NULL)) {
                    mask |= 1 << j;
                    values[j] = (String) innerArray.get(j);
                }
            }
            map[i] = new OneEntry(mask, values);
        }
        return map;
    }

    private void findFull(OneEntry[] map, int startIndex, ArrayList<String[]> listToCollect, byte currentMask, ArrayList<ArrayList<String[]>> outputList) {
        if (currentMask == maskLength) {
            outputList.add(listToCollect);
        } else {
            for (int i = startIndex; i < map.length; i++) {
                ArrayList<String[]> clone = new ArrayList<>(listToCollect);
                if ((currentMask ^ map[i].getMask()) == maskLength) {
                    clone.add(map[i].getValues());
                    outputList.add(clone);
                } else if ((currentMask & map[i].getMask()) == 0) {
                    clone.add(map[i].getValues());
                    findFull(map, i + 1, clone, (byte) (currentMask | map[i].getMask()), outputList);
                }
            }
        }
    }
}
