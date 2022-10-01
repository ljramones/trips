package com.teamgannon.trips.file.compact;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.teamgannon.trips.jpa.model.StarObject;

import java.util.UUID;

public class StarObjectSerializer extends Serializer<StarObject> {


    @Override
    public void write(Kryo kryo, Output output, StarObject starObject) {

        // UUID
        output.writeString(starObject.getId());

        // dataset name
        output.writeString(starObject.getDataSetName());

        // displayName
        output.writeString(starObject.getDisplayName());

        // constellationName
        output.writeString(starObject.getConstellationName());

        // mass
        output.writeDouble(starObject.getMass());

        // notes
        output.writeString(starObject.getNotes());

        // source
        output.writeString(starObject.getSource());

        // catalogIdList
        output.writeString(starObject.getRawCatalogIdList());

        // x, y, z
        output.writeDouble(starObject.getX());
        output.writeDouble(starObject.getY());
        output.writeDouble(starObject.getZ());

        // radius, ra, pmra, declination, pmdec, parallax, distance, radialVelocity
        output.writeDouble(starObject.getRadius());
        output.writeDouble(starObject.getRa());
        output.writeDouble(starObject.getPmra());
        output.writeDouble(starObject.getDeclination());
        output.writeDouble(starObject.getPmdec());
        output.writeDouble(starObject.getParallax());
        output.writeDouble(starObject.getDistance());
        output.writeDouble(starObject.getRadialVelocity());

        // spectralclass, orthoSpectralClass
        output.writeString(starObject.getSpectralClass());
        output.writeString(starObject.getOrthoSpectralClass());

        // temperature
        output.writeDouble(starObject.getTemperature());

        // realstar
        output.writeBoolean(starObject.isRealStar());

        // bprp, bpg, grp
        output.writeDouble(starObject.getBprp());
        output.writeDouble(starObject.getBpg());
        output.writeDouble(starObject.getGrp());

        // luminosity
        output.writeString(starObject.getLuminosity());

        // magu, magb, magv, magr, magi
        output.writeDouble(starObject.getMagu());
        output.writeDouble(starObject.getMagb());
        output.writeDouble(starObject.getMagv());
        output.writeDouble(starObject.getMagr());
        output.writeDouble(starObject.getMagi());

        // other, anomaly,
        output.writeBoolean(starObject.isOther());
        output.writeBoolean(starObject.isAnomaly());

        // polity, worldtype, fuelType, portType,
        // populationType, TechType, productType, milspaceType,
        // milplanType, commonName, simbadId
        output.writeString(starObject.getPolity());
        output.writeString(starObject.getWorldType());
        output.writeString(starObject.getFuelType());
        output.writeString(starObject.getPortType());
        output.writeString(starObject.getPopulationType());
        output.writeString(starObject.getTechType());
        output.writeString(starObject.getProductType());
        output.writeString(starObject.getMilSpaceType());
        output.writeString(starObject.getMilPlanType());
        output.writeString(starObject.getCommonName());
        output.writeString(starObject.getSimbadId());

        // age, metallicity, galacticLat, galacticLong
        output.writeDouble(starObject.getAge());
        output.writeDouble(starObject.getMetallicity());
        output.writeDouble(starObject.getGalacticLat());
        output.writeDouble(starObject.getGalacticLong());

        // gaiaId
        output.writeString(starObject.getGaiaId());

        // miscText1, miscText2, miscText3, miscText4, miscText5
        output.writeString(starObject.getMiscText1());
        output.writeString(starObject.getMiscText2());
        output.writeString(starObject.getMiscText3());
        output.writeString(starObject.getMiscText4());
        output.writeString(starObject.getMiscText5());

        // miscNum1, miscNum2, miscNum3, miscNum4, miscNum5
        output.writeDouble(starObject.getMiscNum1());
        output.writeDouble(starObject.getMiscNum2());
        output.writeDouble(starObject.getMiscNum3());
        output.writeDouble(starObject.getMiscNum4());
        output.writeDouble(starObject.getMiscNum5());

    }

    @Override
    public StarObject read(Kryo kryo, Input input, Class<? extends StarObject> aClass) {
        StarObject starObject = new StarObject();

        // reconstruct the UUID
        starObject.setId(input.readString());

        // dataset
        starObject.setDataSetName(input.readString());

        // displayName
        starObject.setDisplayName(input.readString());

        // constellationName
        starObject.setConstellationName(input.readString());

        // mass
        starObject.setMass(input.readDouble());

        // notes
        starObject.setNotes(input.readString());

        // source
        starObject.setSource(input.readString());

        // catalogIdList
        starObject.setCatalogIdList(input.readString());

        // x, y ,z
        starObject.setX(input.readDouble());
        starObject.setY(input.readDouble());
        starObject.setZ(input.readDouble());

        // radius, ra, pmra, declination, pmdec, parallax, distance, radialVelocity
        starObject.setRadius(input.readDouble());
        starObject.setRa(input.readDouble());
        starObject.setPmra(input.readDouble());
        starObject.setDeclination(input.readDouble());
        starObject.setPmdec(input.readDouble());
        starObject.setParallax(input.readDouble());
        starObject.setDistance(input.readDouble());
        starObject.setRadialVelocity(input.readDouble());

        // spectralclass, orthoSpectralClass
        starObject.setSpectralClass(input.readString());
        starObject.setOrthoSpectralClass(input.readString());

        // temperature
        starObject.setTemperature(input.readDouble());

        // realstar
        starObject.setRealStar(input.readBoolean());

        // bprp, bpg, grp
        starObject.setBprp(input.readDouble());
        starObject.setBpg(input.readDouble());
        starObject.setGrp(input.readDouble());

        // luminosity
        starObject.setLuminosity(input.readString());

        // magu, magb, magv, magr, magi
        starObject.setMagu(input.readDouble());
        starObject.setMagb(input.readDouble());
        starObject.setMagv(input.readDouble());
        starObject.setMagr(input.readDouble());
        starObject.setMagi(input.readDouble());

        // other, anomaly,
        starObject.setOther(input.readBoolean());
        starObject.setAnomaly(input.readBoolean());

        // polity, worldtype, fuelType, portType,
        // populationType, TechType, productType, milSpaceType,
        // milplanType, commonName, simbadId
        starObject.setPolity(input.readString());
        starObject.setWorldType(input.readString());
        starObject.setFuelType(input.readString());
        starObject.setPortType(input.readString());
        starObject.setPopulationType(input.readString());
        starObject.setTechType(input.readString());
        starObject.setProductType(input.readString());
        starObject.setMilSpaceType(input.readString());
        starObject.setMilPlanType(input.readString());
        starObject.setCommonName(input.readString());
        starObject.setSimbadId(input.readString());

        // age, metallicity, galacticLat, galacticLong
        starObject.setAge(input.readDouble());
        starObject.setMetallicity(input.readDouble());
        starObject.setGalacticLat(input.readDouble());
        starObject.setGalacticLong(input.readDouble());

        // gaiaId
        starObject.setGaiaId(input.readString());

        // miscText1, miscText2, miscText3, miscText4, miscText5
        starObject.setMiscText1(input.readString());
        starObject.setMiscText2(input.readString());
        starObject.setMiscText3(input.readString());
        starObject.setMiscText4(input.readString());
        starObject.setMiscText5(input.readString());

        // miscNum1, miscNum2, miscNum3, miscNum4, miscNum5
        starObject.setMiscNum1(input.readDouble());
        starObject.setMiscNum2(input.readDouble());
        starObject.setMiscNum3(input.readDouble());
        starObject.setMiscNum4(input.readDouble());
        starObject.setMiscNum5(input.readDouble());

        return starObject;
    }

}
