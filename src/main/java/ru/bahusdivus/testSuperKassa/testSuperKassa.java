package ru.bahusdivus.testSuperKassa;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class testSuperKassa {
    private JSONArray jsonInput;
    private int length;
    private byte maskLength;
    private JSONArray outputJson;
    private ClassLoader classloader;

    public static void main(String[] args) {
        testSuperKassa tsk = new testSuperKassa();
        tsk.load("input.json");
        tsk.makeCompliteRoutes();
        tsk.save("output.json");
    }

    private testSuperKassa() {
        classloader = Thread.currentThread().getContextClassLoader();
    }

    private void load(String fileName) {
        StringBuilder input = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream(fileName)), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                input.append(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        jsonInput = new JSONArray(input.toString());
        length = ((JSONArray) jsonInput.get(0)).length();
        maskLength = (byte) (Math.pow(2, length) - 1);
    }

    private void makeCompliteRoutes() {

        OneEntry[] map = jsonToEntryArray(jsonInput);

        ArrayList<ArrayList<String[]>> outputList = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            ArrayList<String[]> list = new ArrayList<>();
            list.add(map[i].getValues());
            findFull(map, i + 1, list, map[i].getMask(), outputList);
        }

        outputJson = new JSONArray();
        for (ArrayList<String[]> collect : outputList) {
            String[] rowArray = new String[length];
            for (int i = 0; i < length; i++) {
                for (String[] strings : collect) {
                    if (strings[i] != null) rowArray[i] = strings[i];
                }
            }
            outputJson.put(rowArray);
        }
    }

    private void save(String fileName) {
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
            String[] values = new String[length];
            byte mask = 0;
            for (int j = 0; j < length; j++) {
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
