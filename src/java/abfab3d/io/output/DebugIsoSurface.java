package abfab3d.io.output;

import toxi.geom.Vec3D;
import toxi.geom.mesh.Mesh3D;
import toxi.geom.mesh.TriangleMesh;
import toxi.volume.IsoSurface;
import toxi.volume.VolumetricSpace;
import toxi.volume.VolumetricSpaceArray;

import java.util.logging.Logger;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class DebugIsoSurface implements IsoSurface {
        protected static final Logger logger = Logger
                .getLogger(DebugIsoSurface.class.getName());

        protected Vec3D cellSize;
        protected Vec3D centreOffset;
        protected VolumetricSpace volume;

        public float isoValue;

        protected int resX, resY, resZ;
        protected int resX1, resY1, resZ1;

        protected int sliceRes;
        protected int nextXY;

        protected Vec3D[] edgeVertices;

        public DebugIsoSurface(VolumetricSpace volume) {
            this.volume = volume;
            cellSize = new Vec3D(volume.scale.x / volume.resX1, volume.scale.y
                    / volume.resY1, volume.scale.z / volume.resZ1);

            resX = volume.resX;
            resY = volume.resY;
            resZ = volume.resZ;
            resX1 = volume.resX1;
            resY1 = volume.resY1;
            resZ1 = volume.resZ1;

            sliceRes = volume.sliceRes;
            nextXY = resX + sliceRes;

            centreOffset = volume.halfScale.getInverted();

            // TODO: removed center, different then orig code
            centreOffset = new Vec3D(0,0,0);

            edgeVertices = new Vec3D[3 * volume.numCells];

            //((VolumetricSpaceArray)volume).printData();
        }

        /**
         * Computes the surface mesh for the given volumetric data and iso value.
         */
        public Mesh3D computeSurfaceMesh(Mesh3D mesh, final float iso) {
            if (mesh == null) {
                mesh = new TriangleMesh("isosurface-" + iso);
            } else {
                mesh.clear();
            }
            isoValue = iso;
            float offsetZ = centreOffset.z;
            
            for (int z = 0; z < resZ1; z++) {
                int sliceOffset = sliceRes * z;
                float offsetY = centreOffset.y;
                for (int y = 0; y < resY1; y++) {
                    float offsetX = centreOffset.x;
                    int offset = resX * y + sliceOffset;
                    for (int x = 0; x < resX1; x++) {
                        final int cellIndex = getCellIndex(x, y, z);
                        if (cellIndex > 0 && cellIndex < 255) {
                            final int edgeFlags = MarchingCubesIndex.edgesToCompute[cellIndex];
                            if (edgeFlags > 0 && edgeFlags < 255) {
                                int edgeOffsetIndex = offset * 3;
                                float offsetData = volume.getVoxelAt(offset);
                                float isoDiff = isoValue - offsetData;
                                
//System.out.println("edge offset: " + (edgeOffsetIndex));
                                if ((edgeFlags & 1) > 0) {
                                    //System.out.println("x: " + x + " y: " + y + " z: " + z + " offsetX: " + offsetX + " centerOffset: " + centreOffset);
                                    if (edgeVertices[edgeOffsetIndex] == null) {
                                        float t = isoDiff
                                                / (volume.getVoxelAt(offset + 1) - offsetData);
                                        edgeVertices[edgeOffsetIndex] = new Vec3D(
                                                offsetX + t * cellSize.x, y
                                                * cellSize.y
                                                + centreOffset.y, z
                                                * cellSize.z
                                                + centreOffset.z);
                                    }
                                }
                                if ((edgeFlags & 2) > 0) {
                                    //System.out.println("x: " + x + " y: " + y + " z: " + z + " offsetY: " + offsetY + " centerOffset: " + centreOffset);

                                    if (edgeVertices[edgeOffsetIndex + 1] == null) {
                                        float t = isoDiff
                                                / (volume.getVoxelAt(offset + resX) - offsetData);
                                        edgeVertices[edgeOffsetIndex + 1] = new Vec3D(
                                                x * cellSize.x + centreOffset.x,
                                                offsetY + t * cellSize.y, z
                                                * cellSize.z
                                                + centreOffset.z);
                                    }
                                }
                                if ((edgeFlags & 4) > 0) {
                                    //System.out.println("x: " + x + " y: " + y + " z: " + z + " offsetZ: " + offsetZ + " centerOffset: " + centreOffset);
                                    if (edgeVertices[edgeOffsetIndex + 2] == null) {
                                        float t = isoDiff
                                                / (volume.getVoxelAt(offset
                                                + sliceRes) - offsetData);
                                        edgeVertices[edgeOffsetIndex + 2] = new Vec3D(
                                                x * cellSize.x + centreOffset.x, y
                                                * cellSize.y
                                                + centreOffset.y, offsetZ
                                                + t * cellSize.z);
                                    }
                                }
                            }
                        }
                        offsetX += cellSize.x;
                        offset++;
                    }
                    offsetY += cellSize.y;
                }
                offsetZ += cellSize.z;
            }

            //System.out.println("verts: " + java.util.Arrays.toString(edgeVertices));

            final int[] face = new int[16];
            for (int z = 0; z < resZ1; z++) {
                int sliceOffset = sliceRes * z;
                for (int y = 0; y < resY1; y++) {
                    int offset = resX * y + sliceOffset;
                    for (int x = 0; x < resX1; x++) {
                        final int cellIndex = getCellIndex(x, y, z);
                        if (cellIndex > 0 && cellIndex < 255) {
                            int n = 0;
                            int edgeIndex;
                            final int[] cellTriangles = MarchingCubesIndex.cellTriangles[cellIndex];
                            while ((edgeIndex = cellTriangles[n]) != -1) {
                                int[] edgeOffsetInfo = MarchingCubesIndex.edgeOffsets[edgeIndex];
                                face[n] = ((x + edgeOffsetInfo[0]) + resX
                                        * (y + edgeOffsetInfo[1]) + sliceRes
                                        * (z + edgeOffsetInfo[2]))
                                        * 3 + edgeOffsetInfo[3];
                                n++;
                            }
                            for (int i = 0; i < n; i += 3) {
                                final Vec3D va = edgeVertices[face[i + 1]];
                                final Vec3D vb = edgeVertices[face[i + 2]];
                                final Vec3D vc = edgeVertices[face[i]];
                                if (va != null && vb != null && vc != null) {
                                    //System.out.println("Add face: " + va + " " + vb + " " + vc);
                                    mesh.addFace(va, vb, vc);
                                }
                            }
                        }
                        offset++;
                    }
                }
            }
            return mesh;
        }

        protected final int getCellIndex(int x, int y, int z) {
            int cellIndex = 0;
            int idx = x + y * resX + z * sliceRes;
            float val;

            val = volume.getVoxelAt(idx);
//System.out.print("vals: " + val);
            if (val < isoValue) {
                cellIndex |= 1;
            }

            val = volume.getVoxelAt(idx + sliceRes);
            //System.out.print(" " + val);
            if (val < isoValue) {
                cellIndex |= 8;
            }
            //System.out.print(" " + val);
            val = volume.getVoxelAt(idx + resX);
            if (val < isoValue) {
                cellIndex |= 16;
            }
            //System.out.print(" " + val);
            val = volume.getVoxelAt(idx + resX + sliceRes);
            if (val < isoValue) {
                cellIndex |= 128;
            }
            idx++;
            //System.out.print(" " + val);
            val = volume.getVoxelAt(idx);
            if (val < isoValue) {
                cellIndex |= 2;
            }
            //System.out.print(" " + val);
            val = volume.getVoxelAt(idx + sliceRes);
            if (val < isoValue) {
                cellIndex |= 4;
            }
            //System.out.print(" " + val);
            val = volume.getVoxelAt(idx + resX);
            if (val < isoValue) {
                cellIndex |= 32;
            }

            val = volume.getVoxelAt(idx + resX + sliceRes);
            //System.out.println(" " + val);

            if (val < isoValue) {
                cellIndex |= 64;
            }

            //System.out.println("gci: " + x + " " + y + " " + z + " cellIndex: " + cellIndex + " bits: " + Integer.toBinaryString(cellIndex));

            return cellIndex;
        }

        /**
         * Resets mesh vertices to default positions and clears face index. Needs to
         * be called inbetween successive calls to
         * {@link #computeSurfaceMesh(Mesh3D, float)}
         */
        public void reset() {
            for (int i = 0; i < edgeVertices.length; i++) {
                edgeVertices[i] = null;
            }
        }
    }
