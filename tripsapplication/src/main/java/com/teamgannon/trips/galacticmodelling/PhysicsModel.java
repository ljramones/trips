package com.teamgannon.trips.galacticmodelling;

import lombok.extern.slf4j.Slf4j;
//import toxi.geom.AABB;
//import toxi.geom.Vec3D;
//import toxi.geom.mesh.*;
//import toxi.physics3d.VerletParticle3D;
//import toxi.physics3d.VerletPhysics3D;
//import toxi.physics3d.VerletSpring3D;
//import toxi.physics3d.behaviors.GravityBehavior3D;

import java.net.MalformedURLException;
import java.net.URL;

//import static toxi.math.MathUtils.HALF_PI;

@Slf4j
public class PhysicsModel {

//    private final VerletPhysics3D physics = new VerletPhysics3D();
//    private WETriangleMesh mesh;
//
//    private AABB bounds;
//
//    private Vec3D ext;
//    private Vec3D min;
//    private Vec3D max;
//
//    public void setup(URL meshModel) {
//        try {
//            mesh = new WETriangleMesh().addMesh(new STLReader().loadBinary(meshModel.openStream(), "car", STLReader.WEMESH));
//            // properly orient and scale mesh
//            mesh.rotateX(HALF_PI);
//            mesh.scale(8);
//            // adjust physics bounding box based on car (but bigger)
//            // and align car with bottom of the new box
//            bounds = mesh.getBoundingBox();
//            ext = bounds.getExtent();
//            min = bounds.sub(ext.scale(4, 3, 2));
//            max = bounds.add(ext.scale(4, 3, 2));
//            physics.setWorldBounds(AABB.fromMinMax(min, max));
//            mesh.translate(new Vec3D(ext.scale(3, 2, 0)));
//            // set gravity along negative X axis with slight downward
//            physics.addBehavior(new GravityBehavior3D(new Vec3D(-0.1f, 0.001f, 0)));
//            // turn mesh vertices into physics particles
//            for (Vertex v : mesh.vertices.values()) {
//                physics.addParticle(new VerletParticle3D(v));
//            }
//            // turn mesh edges into springs
//            for (WingedEdge e : mesh.edges.values()) {
//                VerletParticle3D a = physics.particles.get(((WEVertex) e.a).id);
//                VerletParticle3D b = physics.particles.get(((WEVertex) e.b).id);
//                physics.addSpring(new VerletSpring3D(a, b, a.distanceTo(b), 1f));
//            }
//        } catch (Exception e) {
//            log.error("failed to inti");
//            e.printStackTrace();
//        }
//    }
//
//    public void setTimeStep(float timestep) {
//        physics.setTimeStep(timestep);
//    }
//
//    public void update() {
//        physics.update();
//        // update mesh vertices based on the current particle positions
//        for (Vertex v : mesh.vertices.values()) {
//            v.set(physics.particles.get(v.id));
//        }
//        // update mesh normals
//        mesh.computeFaceNormals();
//    }

    public static void main(String[] args) {
//        PhysicsModel physicsModel = new PhysicsModel();
//        try {
//            long start = System.currentTimeMillis();
//            physicsModel.setup(new URL("file:///Users/larrymitchell/tripsnew/trips/files/physicsModels/audi.stl"));
//            physicsModel.setTimeStep(50);
//            long end = System.currentTimeMillis();
//            log.info("time for initialization={} ms", end - start);
//            for (int i = 0; i < 20; i++) {
//                long startIt = System.currentTimeMillis();
//                physicsModel.update();
//                long endIt = System.currentTimeMillis();
//                log.info("time for update={} ms", endIt - startIt);
//            }
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        log.info("done");
    }

}
