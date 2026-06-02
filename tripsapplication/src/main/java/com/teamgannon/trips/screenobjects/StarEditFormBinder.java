package com.teamgannon.trips.screenobjects;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import static com.teamgannon.trips.screenobjects.StarFieldValidator.*;

/**
 * Handles binding form fields to/from a {@link StarEditViewModel} (Issue 23 /
 * Phase 7-follow-on).
 * <p>
 * Previously this class mutated a live JPA {@link com.teamgannon.trips.jpa.model.StarObject}
 * which caused {@code LazyInitializationException} when the dialog accessed
 * lazy collections (notably {@code aliasList}) outside a transaction. The
 * binder now works against a plain detached POJO. The dialog's caller is
 * responsible for converting between the entity and the view-model via
 * {@link StarEditMapper}, which makes the alias initialise safely inside
 * a transactional service before the view-model crosses to the FX thread.
 */
public class StarEditFormBinder {

    private final StarEditViewModel vm;

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

    // Fictional worldbuilding fields removed by the Worldbuilding Data Model Normalization
    // task. The widgets, view-model fields, and combo-box config all came down together.

    // User fields
    // misc text/num text fields removed with V5 schema cleanup (Issue 31/54).

    // Checkboxes
    private CheckBox forceLabel;

    public StarEditFormBinder(@NotNull StarEditViewModel vm) {
        this.vm = vm;
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
     * Set the user custom fields.
     */
    public void setUserFields(CheckBox force) {
        this.forceLabel = force;
    }

    /**
     * Initialize overview tab with data from the view-model.
     */
    public void initializeOverviewTab(Label recordIdLabel, Label dataSetLabel) {
        recordIdLabel.setText(vm.getId());
        dataSetLabel.setText(vm.getDataSetName());

        starNameTextField.setText(vm.getDisplayName());
        commonNameTextField.setText(vm.getCommonName());
        constellationNameTextField.setText(vm.getConstellationName());

        setupTextField(spectralClassTextField, vm.getSpectralClass(), "the spectral class as in O, A, etc.");

        setupDoubleField(distanceNameTextField, vm.getDistance(),
                "the distance from Sol in ly, press enter", () -> parseDouble(distanceNameTextField, vm::setDistance));

        setupDoubleField(metallicityTextfield, vm.getMetallicity(),
                "the metallicity, press enter", () -> parseDouble(metallicityTextfield, vm::setMetallicity));

        setupDoubleField(ageTextfield, vm.getAge(),
                "the age, press enter", () -> parseDouble(ageTextfield, vm::setAge));

        setupDoubleField(xTextField, vm.getX(),
                "X coordinate, press enter", () -> parseDouble(xTextField, vm::setX));

        setupDoubleField(yTextField, vm.getY(),
                "Y coordinate, press enter", () -> parseDouble(yTextField, vm::setY));

        setupDoubleField(zTextField, vm.getZ(),
                "Z coordinate, press enter", () -> parseDouble(zTextField, vm::setZ));

        notesArea.setText(vm.getNotes());
        notesArea.setPromptText("Enter a description or general notes on this star");
    }

    /**
     * Initialize secondary (scientific) tab with data from the view-model.
     */
    public void initializeSecondaryTab() {
        setupTextField(simbadIdTextField, vm.getSimbadId(), "the Simbad Id");

        setupDoubleField(galacticCoorLatTextField, vm.getGalacticLat(),
                "the galactic latitude", () -> parseDouble(galacticCoorLatTextField, vm::setGalacticLat));

        setupDoubleField(galacticCoorLongTextField, vm.getGalacticLong(),
                "the galactic longitude", () -> parseDouble(galacticCoorLongTextField, vm::setGalacticLong));

        setupDoubleField(radiusTextField, vm.getRadius(),
                "the radius in Sol units", () -> parseDouble(radiusTextField, vm::setRadius));

        setupDoubleField(massTextField, vm.getMass(),
                "the mass in Sol units", () -> parseDouble(massTextField, vm::setMass));

        luminosityTextField.setText(vm.getLuminosity() != null ? vm.getLuminosity() : "");
        luminosityTextField.setPromptText("luminosity value or class (e.g., 1.0 or V)");
        luminosityTextField.setOnKeyPressed(ke -> {
            if (ke.getCode() == javafx.scene.input.KeyCode.ENTER) {
                vm.setLuminosity(luminosityTextField.getText());
            }
        });

        setupDoubleField(tempTextField, vm.getTemperature(),
                "the surface temperature of the star", () -> parseDouble(tempTextField, vm::setTemperature));

        setupDoubleField(raLabel, vm.getRa(),
                "right ascension, press enter", () -> parseDouble(raLabel, vm::setRa));

        setupDoubleField(decLabel, vm.getDeclination(),
                "declination, press enter", () -> parseDouble(decLabel, vm::setDeclination));

        setupDoubleField(pmraLabel, vm.getPmra(),
                "PMRA, press enter", () -> parseDouble(pmraLabel, vm::setPmra));

        setupDoubleField(pmdecLabel, vm.getPmdec(),
                "PMDEC, press enter", () -> parseDouble(pmdecLabel, vm::setPmdec));

        setupDoubleField(parallaxLabel, vm.getParallax(),
                "parallax, press enter", () -> parseDouble(parallaxLabel, vm::setParallax));

        setupDoubleField(radialVelocityLabel, vm.getRadialVelocity(),
                "radial velocity, press enter", () -> parseDouble(radialVelocityLabel, vm::setRadialVelocity));

        setupDoubleField(bprpLabel, vm.getBprp(),
                "bprp, press enter", () -> parseDouble(bprpLabel, vm::setBprp));

        setupDoubleField(bpgLabel, vm.getBpg(),
                "bpg, press enter", () -> parseDouble(bpgLabel, vm::setBpg));

        setupDoubleField(grpLabel, vm.getGrp(),
                "grp, press enter", () -> parseDouble(grpLabel, vm::setGrp));

        setupDoubleField(maguTextField, vm.getMagu(),
                "magu, press enter", () -> parseDouble(maguTextField, vm::setMagu));

        setupDoubleField(magbTextField, vm.getMagb(),
                "magb, press enter", () -> parseDouble(magbTextField, vm::setMagb));

        setupDoubleField(magvTextField, vm.getMagv(),
                "magv, press enter", () -> parseDouble(magvTextField, vm::setMagv));

        setupDoubleField(magrTextField, vm.getMagr(),
                "magr, press enter", () -> parseDouble(magrTextField, vm::setMagr));

        setupDoubleField(magiTextField, vm.getMagi(),
                "magi, press enter", () -> parseDouble(magiTextField, vm::setMagi));

        gaiaIdTextField.setText(vm.getGaiaDR2CatId());

        // Aliases are eagerly materialised on the VM — no LIE risk.
        if (!vm.getAliases().isEmpty()) {
            aliasTextArea.setText(String.join(", ", vm.getAliases()));
        }
    }

    /**
     * Initialize the user-tab CheckBox (the only surviving control after
     * V5 dropped the misc text/num scratch fields).
     */
    public void initializeUserTab() {
        forceLabel.setSelected(vm.isForceLabelToBeShown());
    }

    /**
     * Collect all data from form fields and update the view-model.
     * Should be called before saving.
     *
     * @throws StarFieldValidationException if any numeric field has invalid data
     */
    public void collectAllData() {
        // Overview
        vm.setDisplayName(starNameTextField.getText());
        vm.setConstellationName(constellationNameTextField.getText());
        vm.setSpectralClass(spectralClassTextField.getText());
        vm.setNotes(notesArea.getText());
        vm.setCommonName(commonNameTextField.getText());

        vm.setRadius(readDouble(radiusTextField, "Radius"));
        vm.setMass(readDouble(massTextField, "Mass"));
        vm.setLuminosity(luminosityTextField.getText());
        vm.setDistance(readDouble(distanceNameTextField, "Distance"));
        vm.setTemperature(readDouble(tempTextField, "Temperature"));
        vm.setX(readDouble(xTextField, "X coordinate"));
        vm.setY(readDouble(yTextField, "Y coordinate"));
        vm.setZ(readDouble(zTextField, "Z coordinate"));
        vm.setMetallicity(readDouble(metallicityTextfield, "Metallicity"));
        vm.setAge(readDouble(ageTextfield, "Age"));

        // Fictional worldbuilding fields removed by the Worldbuilding Data Model
        // Normalization task — no save-side collection needed.

        // Secondary/Scientific
        vm.setRa(readDouble(raLabel, "Right ascension"));
        vm.setPmra(readDouble(pmraLabel, "PMRA"));
        vm.setDeclination(readDouble(decLabel, "Declination"));
        vm.setPmdec(readDouble(pmdecLabel, "PMDEC"));
        vm.setParallax(readDouble(parallaxLabel, "Parallax"));
        vm.setRadialVelocity(readDouble(radialVelocityLabel, "Radial velocity"));
        vm.setBprp(readDouble(bprpLabel, "BP-RP"));
        vm.setBpg(readDouble(bpgLabel, "BP-G"));
        vm.setGrp(readDouble(grpLabel, "G-RP"));
        vm.setSimbadId(simbadIdTextField.getText());
        vm.setGalacticLat(readDouble(galacticCoorLatTextField, "Galactic latitude"));
        vm.setGalacticLong(readDouble(galacticCoorLongTextField, "Galactic longitude"));

        vm.setMagu(readDouble(maguTextField, "U magnitude"));
        vm.setMagb(readDouble(magbTextField, "B magnitude"));
        vm.setMagv(readDouble(magvTextField, "V magnitude"));
        vm.setMagr(readDouble(magrTextField, "R magnitude"));
        vm.setMagi(readDouble(magiTextField, "I magnitude"));

        vm.setGaiaDR2CatId(gaiaIdTextField.getText());

        vm.setForceLabelToBeShown(forceLabel.isSelected());
    }

    /**
     * Get the view-model being edited.
     */
    public StarEditViewModel getViewModel() {
        return vm;
    }

    private static double readDouble(TextField textField, String fieldLabel) {
        return parseDoubleOrThrow(textField, fieldLabel);
    }
}
