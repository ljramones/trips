package com.teamgannon.trips.file.compact;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;

public class DataSetDescriptorSerializer  extends Serializer<DataSetDescriptor> {

    @Override
    public void write(Kryo kryo, Output output, DataSetDescriptor dataSetDescriptor) {
        output.writeString(dataSetDescriptor.getDataSetName());
        output.writeString(dataSetDescriptor.getFilePath());
        output.writeString(dataSetDescriptor.getFileCreator());
        output.writeLong(dataSetDescriptor.getFileOriginalDate());
        output.writeString(dataSetDescriptor.getFileNotes());
        output.writeString(dataSetDescriptor.getDatasetType());
        output.writeLong(dataSetDescriptor.getNumberStars());
        output.writeDouble(dataSetDescriptor.getDistanceRange());
        output.writeInt(dataSetDescriptor.getNumberRoutes());
        output.writeString(dataSetDescriptor.getThemeStr());
        output.writeString(dataSetDescriptor.getAstroDataString());
        output.writeString(dataSetDescriptor.getRoutesStr());
        output.writeString(dataSetDescriptor.getCustomDataDefsStr());
        output.writeString(dataSetDescriptor.getCustomDataValuesStr());
        output.writeString(dataSetDescriptor.getTransitPreferencesStr());

    }

    @Override
    public DataSetDescriptor read(Kryo kryo, Input input, Class<? extends DataSetDescriptor> aClass) {
        DataSetDescriptor descriptor = new DataSetDescriptor();

        descriptor.setDataSetName(input.readString());
        descriptor.setFilePath(input.readString());
        descriptor.setFileCreator(input.readString());
        descriptor.setFileOriginalDate(input.readLong());
        descriptor.setFileNotes(input.readString());
        descriptor.setDatasetType(input.readString());
        descriptor.setNumberStars(input.readLong());
        descriptor.setDistanceRange(input.readDouble());
        descriptor.setNumberRoutes(input.readInt());
        descriptor.setThemeStr(input.readString());
        descriptor.setAstroDataString(input.readString());
        descriptor.setRoutesStr(input.readString());
        descriptor.setCustomDataDefsStr(input.readString());
        descriptor.setCustomDataValuesStr(input.readString());
        descriptor.setTransitPreferencesStr(input.readString());

        return descriptor;
    }

}
