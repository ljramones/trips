package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import static com.teamgannon.trips.screenobjects.StarFieldValidator.*;

/**
 * Handles binding form fields to/from a StarObject record.
 * Separates the data transfer logic from the dialog UI wiring.
 */
public class StarEditFormBinder {

    private final StarObject record;

    // Overview fields
    private TextField starNameTextField;
    private TextField commonNameTextField;
    private TextField constellationNameTextField;
    private TextField spectralClassTextField;
    private TextField distanceNameTextField;
    private TextField metallicityTextfield;
    private TextField ageTextfield;
    private TextField xTextField;
    private TextField yTextField;
    private TextField zTextField;
    private TextArea notesArea;

    // Secondary/Scientific fields
    private TextField simbadIdTextField;
    private TextField galacticCoorLatTextField;
    private TextField galacticCoorLongTextField;
    private TextField radiusTextField;
    private TextField massTextField;
    private TextField luminosityTextField;
    private TextField tempTextField;
    private TextField raLabel;
    private TextField decLabel;
    private TextField pmraLabel;
    private TextField pmdecLabel;
    private TextField parallaxLabel;
    private TextField radialVelocityLabel;
    private TextField bprpLabel;
    private TextField bpgLabel;
    private TextField grpLabel;
    private TextField maguTextField;
    private TextField magbTextField;
    private TextField magvTextField;
    private TextField magrTextField;
    private TextField magiTextField;
    private TextField gaiaIdTextField;
    private TextArea aliasTextArea;

    // Fictional fields
    private TextField polityTextField;
    private TextField worldTypeTextField;
    private TextField fuelTypeTextField;
    private TextField techTypeTextField;
    private TextField portTypeTextField;
    private TextField popTypeTextField;
    private TextField prodField;
    private TextField milspaceTextField;
    private TextField milplanTextField;

    // User fields
    private TextField misc1TextField;
    private TextField misc2TextField;
    private TextField misc3TextField;
    private TextField misc4TextField;
    private TextField misc5TextField;
    private TextField miscNum1TextField;
    private TextField miscNum2TextField;
    private TextField miscNum3TextField;
    private TextField miscNum4TextField;
    private TextField miscNum5TextField;

    // Checkboxes
    private CheckBox forceLabel;

    public StarEditFormBinder(@NotNull StarObject record) {
        this.record = record;
    }

    /**
     * Set the overview tab fields.
     */
    public void setOverviewFields(TextField starName, TextField commonName, TextField constellation,
                                  TextField spectral, TextField distance, TextField metallicity,
                                  TextField age, TextField x, TextField y, TextField z, TextArea notes) {
        this.starNameTextField = starName;
        this.commonNameTextField = commonName;
        this.constellationNameTextField = constellation;
        this.spectralClassTextField = spectral;
        this.distanceNameTextField = distance;
        this.metallicityTextfield = metallicity;
        this.ageTextfield = age;
        this.xTextField = x;
        this.yTextField = y;
        this.zTextField = z;
        this.notesArea = notes;
    }

    /**
     * Set the secondary (scientific) tab fields.
     */
    public void setSecondaryFields(TextField simbadId, TextField galLat, TextField galLong,
                                   TextField radius, TextField mass, TextField luminosity,
                                   TextField temp, TextField ra, TextField dec,
                                   TextField pmra, TextField pmdec, TextField parallax,
                                   TextField radialVel, TextField bprp, TextField bpg,
                                   TextField grp, TextField magu, TextField magb,
                                   TextField magv, TextField magr, TextField magi,
                                   TextField gaiaId, TextArea alias) {
        this.simbadIdTextField = simbadId;
        this.galacticCoorLatTextField = galLat;
        this.galacticCoorLongTextField = galLong;
        this.radiusTextField = radius;
        this.massTextField = mass;
        this.luminosityTextField = luminosity;
        this.tempTextField = temp;
        this.raLabel = ra;
        this.decLabel = dec;
        this.pmraLabel = pmra;
        this.pmdecLabel = pmdec;
        this.parallaxLabel = parallax;
        this.radialVelocityLabel = radialVel;
        this.bprpLabel = bprp;
        this.bpgLabel = bpg;
        this.grpLabel = grp;
        this.maguTextField = magu;
        this.magbTextField = magb;
        this.magvTextField = magv;
        this.magrTextField = magr;
        this.magiTextField = magi;
        this.gaiaIdTextField = gaiaId;
        this.aliasTextArea = alias;
    }

    /**
     * Set the fictional tab fields.
     */
    public void setFictionalFields(TextField polity, TextField world, TextField fuel,
                                   TextField tech, TextField port, TextField pop,
                                   TextField prod, TextField milSpace, TextField milPlan) {
        this.polityTextField = polity;
        this.worldTypeTextField = world;
        this.fuelTypeTextField = fuel;
        this.techTypeTextField = tech;
        this.portTypeTextField = port;
        this.popTypeTextField = pop;
        this.prodField = prod;
        this.milspaceTextField = milSpace;
        this.milplanTextField = milPlan;
    }

    /**
     * Set the user custom fields.
     */
    public void setUserFields(TextField misc1, TextField misc2, TextField misc3,
                              TextField misc4, TextField misc5,
                              TextField miscNum1, TextField miscNum2, TextField miscNum3,
                              TextField miscNum4, TextField miscNum5, CheckBox force) {
        this.misc1TextField = misc1;
        this.misc2TextField = misc2;
        this.misc3TextField = misc3;
        this.misc4TextField = misc4;
        this.misc5TextField = misc5;
        this.miscNum1TextField = miscNum1;
        this.miscNum2TextField = miscNum2;
        this.miscNum3TextField = miscNum3;
        this.miscNum4TextField = miscNum4;
        this.miscNum5TextField = miscNum5;
        this.forceLabel = force;
    }

    /**
     * Initialize overview tab with data from record.
     */
    public void initializeOverviewTab(Label recordIdLabel, Label dataSetLabel) {
        recordIdLabel.setText(record.getId().toString());
        dataSetLabel.setText(record.getDataSetName());

        starNameTextField.setText(record.getDisplayName());
        commonNameTextField.setText(record.getCommonName());
        constellationNameTextField.setText(record.getConstellationName());

        setupTextField(spectralClassTextField, record.getSpectralClass(), "the spectral class as in O, A, etc.");

        setupDoubleField(distanceNameTextField, record.getDistance(),
                "the distance from Sol in ly, press enter", () -> parseDouble(distanceNameTextField, record::setDistance));

        setupDoubleField(metallicityTextfield, record.getMetallicity(),
                "the metallicity, press enter", () -> parseDouble(metallicityTextfield, record::setMetallicity));

        setupDoubleField(ageTextfield, record.getAge(),
                "the age, press enter", () -> parseDouble(ageTextfield, record::setAge));

        setupDoubleField(xTextField, record.getX(),
                "X coordinate, press enter", () -> parseDouble(xTextField, record::setX));

        setupDoubleField(yTextField, record.getY(),
                "Y coordinate, press enter", () -> parseDouble(yTextField, record::setY));

        setupDoubleField(zTextField, record.getZ(),
                "Z coordinate, press enter", () -> parseDouble(zTextField, record::setZ));

        notesArea.setText(record.getNotes());
        notesArea.setPromptText("Enter a description or general notes on this star");
    }

    /**
     * Initialize fictional tab with data from record.
     */
    public void initializeFictionalTab() {
        polityTextField.setText(record.getPolity());
        worldTypeTextField.setText(record.getWorldType());
        fuelTypeTextField.setText(record.getFuelType());
        techTypeTextField.setText(record.getTechType());
        portTypeTextField.setText(record.getPortType());
        popTypeTextField.setText(record.getPopulationType());
        prodField.setText(record.getProductType());
        milspaceTextField.setText(record.getMilSpaceType());
        milplanTextField.setText(record.getMilPlanType());
    }

    /**
     * Initialize secondary (scientific) tab with data from record.
     */
    public void initializeSecondaryTab() {
        setupTextField(simbadIdTextField, record.getSimbadId(), "the Simbad Id");

        setupDoubleField(galacticCoorLatTextField, record.getGalacticLat(),
                "the galactic latitude", () -> parseDouble(galacticCoorLatTextField, record::setGalacticLat));

        setupDoubleField(galacticCoorLongTextField, record.getGalacticLong(),
                "the galactic longitude", () -> parseDouble(galacticCoorLongTextField, record::setGalacticLong));

        setupDoubleField(radiusTextField, record.getRadius(),
                "the radius in Sol units", () -> parseDouble(radiusTextField, record::setRadius));

        setupDoubleField(massTextField, record.getMass(),
                "the mass in Sol units", () -> parseDouble(massTextField, record::setMass));

        luminosityTextField.setText(record.getLuminosity() != null ? record.getLuminosity() : "");
        luminosityTextField.setPromptText("luminosity value or class (e.g., 1.0 or V)");
        luminosityTextField.setOnKeyPressed(ke -> {
            if (ke.getCode() == javafx.scene.input.KeyCode.ENTER) {
                record.setLuminosity(luminosityTextField.getText());
            }
        });

        setupDoubleField(tempTextField, record.getTemperature(),
                "the surface temperature of the star", () -> parseDouble(tempTextField, record::setTemperature));

        setupDoubleField(raLabel, record.getRa(),
                "right ascension, press enter", () -> parseDouble(raLabel, record::setRa));

        setupDoubleField(decLabel, record.getDeclination(),
                "declination, press enter", () -> parseDouble(decLabel, record::setDeclination));

        setupDoubleField(pmraLabel, record.getPmra(),
                "PMRA, press enter", () -> parseDouble(pmraLabel, record::setPmra));

        setupDoubleField(pmdecLabel, record.getPmdec(),
                "PMDEC, press enter", () -> parseDouble(pmdecLabel, record::setPmdec));

        setupDoubleField(parallaxLabel, record.getParallax(),
                "parallax, press enter", () -> parseDouble(parallaxLabel, record::setParallax));

        setupDoubleField(radialVelocityLabel, record.getRadialVelocity(),
                "radial velocity, press enter", () -> parseDouble(radialVelocityLabel, record::setRadialVelocity));

        setupDoubleField(bprpLabel, record.getBprp(),
                "bprp, press enter", () -> parseDouble(bprpLabel, record::setBprp));

        setupDoubleField(bpgLabel, record.getBpg(),
                "bpg, press enter", () -> parseDouble(bpgLabel, record::setBpg));

        setupDoubleField(grpLabel, record.getGrp(),
                "grp, press enter", () -> parseDouble(grpLabel, record::setGrp));

        setupDoubleField(maguTextField, record.getMagu(),
                "magu, press enter", () -> parseDouble(maguTextField, record::setMagu));

        setupDoubleField(magbTextField, record.getMagb(),
                "magb, press enter", () -> parseDouble(magbTextField, record::setMagb));

        setupDoubleField(magvTextField, record.getMagv(),
                "magv, press enter", () -> parseDouble(magvTextField, record::setMagv));

        setupDoubleField(magrTextField, record.getMagr(),
                "magr, press enter", () -> parseDouble(magrTextField, record::setMagr));

        setupDoubleField(magiTextField, record.getMagi(),
                "magi, press enter", () -> parseDouble(magiTextField, record::setMagi));

        gaiaIdTextField.setText(record.getGaiaDR2CatId());

        if (!record.getAliasList().isEmpty()) {
            aliasTextArea.setText(String.join(", ", record.getAliasList()));
        }
    }

    /**
     * Initialize user custom fields tab.
     */
    public void initializeUserTab() {
        misc1TextField.setText(record.getMiscText1());
        misc2TextField.setText(record.getMiscText2());
        misc3TextField.setText(record.getMiscText3());
        misc4TextField.setText(record.getMiscText4());
        misc5TextField.setText(record.getMiscText5());

        setupDoubleField(miscNum1TextField, record.getMiscNum1(),
                null, () -> parseDouble(miscNum1TextField, record::setMiscNum1));

        setupDoubleField(miscNum2TextField, record.getMiscNum2(),
                null, () -> parseDouble(miscNum2TextField, record::setMiscNum2));

        setupDoubleField(miscNum3TextField, record.getMiscNum3(),
                null, () -> parseDouble(miscNum3TextField, record::setMiscNum3));

        setupDoubleField(miscNum4TextField, record.getMiscNum4(),
                null, () -> parseDouble(miscNum4TextField, record::setMiscNum4));

        setupDoubleField(miscNum5TextField, record.getMiscNum5(),
                null, () -> parseDouble(miscNum5TextField, record::setMiscNum5));

        forceLabel.setSelected(record.isForceLabelToBeShown());
    }

    /**
     * Collect all data from form fields and update the record.
     * Should be called before saving.
     *
     * @throws NumberFormatException if any numeric field has invalid data
     */
    public void collectAllData() {
        // Overview
        record.setDisplayName(starNameTextField.getText());
        record.setConstellationName(constellationNameTextField.getText());
        record.setSpectralClass(spectralClassTextField.getText());
        record.setNotes(notesArea.getText());
        record.setCommonName(commonNameTextField.getText());

        record.setRadius(parseDoubleOrThrow(radiusTextField));
        record.setMass(parseDoubleOrThrow(massTextField));
        record.setLuminosity(luminosityTextField.getText());
        record.setDistance(parseDoubleOrThrow(distanceNameTextField));
        record.setTemperature(parseDoubleOrThrow(tempTextField));
        record.setX(parseDoubleOrThrow(xTextField));
        record.setY(parseDoubleOrThrow(yTextField));
        record.setZ(parseDoubleOrThrow(zTextField));
        record.setMetallicity(parseDoubleOrThrow(metallicityTextfield));
        record.setAge(parseDoubleOrThrow(ageTextfield));

        // Fictional
        record.setPolity(polityTextField.getText());
        record.setWorldType(worldTypeTextField.getText());
        record.setFuelType(fuelTypeTextField.getText());
        record.setTechType(techTypeTextField.getText());
        record.setPortType(portTypeTextField.getText());
        record.setPopulationType(popTypeTextField.getText());
        record.setProductType(prodField.getText());
        record.setMilSpaceType(milspaceTextField.getText());
        record.setMilPlanType(milplanTextField.getText());

        // Secondary/Scientific
        record.setRa(parseDoubleOrThrow(raLabel));
        record.setPmra(parseDoubleOrThrow(pmraLabel));
        record.setDeclination(parseDoubleOrThrow(decLabel));
        record.setPmdec(parseDoubleOrThrow(pmdecLabel));
        record.setParallax(parseDoubleOrThrow(parallaxLabel));
        record.setRadialVelocity(parseDoubleOrThrow(radialVelocityLabel));
        record.setBprp(parseDoubleOrThrow(bprpLabel));
        record.setBpg(parseDoubleOrThrow(bpgLabel));
        record.setGrp(parseDoubleOrThrow(grpLabel));
        record.setSimbadId(simbadIdTextField.getText());
        record.setGalacticLat(parseDoubleOrThrow(galacticCoorLatTextField));
        record.setGalacticLong(parseDoubleOrThrow(galacticCoorLongTextField));

        record.setMagu(parseDoubleOrThrow(maguTextField));
        record.setMagb(parseDoubleOrThrow(magbTextField));
        record.setMagv(parseDoubleOrThrow(magvTextField));
        record.setMagr(parseDoubleOrThrow(magrTextField));
        record.setMagi(parseDoubleOrThrow(magiTextField));

        record.setGaiaDR2CatId(gaiaIdTextField.getText());

        // User fields
        record.setMiscText1(misc1TextField.getText());
        record.setMiscText2(misc2TextField.getText());
        record.setMiscText3(misc3TextField.getText());
        record.setMiscText4(misc4TextField.getText());
        record.setMiscText5(misc5TextField.getText());

        record.setMiscNum1(parseDoubleOrThrow(miscNum1TextField));
        record.setMiscNum2(parseDoubleOrThrow(miscNum2TextField));
        record.setMiscNum3(parseDoubleOrThrow(miscNum3TextField));
        record.setMiscNum4(parseDoubleOrThrow(miscNum4TextField));
        record.setMiscNum5(parseDoubleOrThrow(miscNum5TextField));

        record.setForceLabelToBeShown(forceLabel.isSelected());
    }

    /**
     * Get the star record being edited.
     */
    public StarObject getRecord() {
        return record;
    }
}
