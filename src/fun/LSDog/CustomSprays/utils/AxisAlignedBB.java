package fun.LSDog.CustomSprays.utils;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import net.minecraft.server.v1_12_R1.Vec3D;

import javax.annotation.Nullable;

public class AxisAlignedBB {

    public final double a;
    public final double b;
    public final double c;
    public final double d;
    public final double e;
    public final double f;

    public AxisAlignedBB(double d0, double d1, double d2, double d3, double d4, double d5) {
        this.a = Math.min(d0, d3);
        this.b = Math.min(d1, d4);
        this.c = Math.min(d2, d5);
        this.d = Math.max(d0, d3);
        this.e = Math.max(d1, d4);
        this.f = Math.max(d2, d5);
    }

    public AxisAlignedBB(BlockPosition blockposition) {
        this((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), (double) (blockposition.getX() + 1), (double) (blockposition.getY() + 1), (double) (blockposition.getZ() + 1));
    }

    public AxisAlignedBB(BlockPosition blockposition, BlockPosition blockposition1) {
        this((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), (double) blockposition1.getX(), (double) blockposition1.getY(), (double) blockposition1.getZ());
    }

    public AxisAlignedBB e(double d0) {
        return new AxisAlignedBB(this.a, this.b, this.c, this.d, d0, this.f);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof AxisAlignedBB)) {
            return false;
        } else {
            AxisAlignedBB axisalignedbb = (AxisAlignedBB) object;

            return Double.compare(axisalignedbb.a, this.a) != 0 ? false : (Double.compare(axisalignedbb.b, this.b) != 0 ? false : (Double.compare(axisalignedbb.c, this.c) != 0 ? false : (Double.compare(axisalignedbb.d, this.d) != 0 ? false : (Double.compare(axisalignedbb.e, this.e) != 0 ? false : Double.compare(axisalignedbb.f, this.f) == 0))));
        }
    }

    public int hashCode() {
        long i = Double.doubleToLongBits(this.a);
        int j = (int) (i ^ i >>> 32);

        i = Double.doubleToLongBits(this.b);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.c);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.d);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.e);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.f);
        j = 31 * j + (int) (i ^ i >>> 32);
        return j;
    }

    public AxisAlignedBB a(double d0, double d1, double d2) {
        double d3 = this.a;
        double d4 = this.b;
        double d5 = this.c;
        double d6 = this.d;
        double d7 = this.e;
        double d8 = this.f;

        if (d0 < 0.0D) {
            d3 -= d0;
        } else if (d0 > 0.0D) {
            d6 -= d0;
        }

        if (d1 < 0.0D) {
            d4 -= d1;
        } else if (d1 > 0.0D) {
            d7 -= d1;
        }

        if (d2 < 0.0D) {
            d5 -= d2;
        } else if (d2 > 0.0D) {
            d8 -= d2;
        }

        return new AxisAlignedBB(d3, d4, d5, d6, d7, d8);
    }

    public AxisAlignedBB b(double d0, double d1, double d2) {
        double d3 = this.a;
        double d4 = this.b;
        double d5 = this.c;
        double d6 = this.d;
        double d7 = this.e;
        double d8 = this.f;

        if (d0 < 0.0D) {
            d3 += d0;
        } else if (d0 > 0.0D) {
            d6 += d0;
        }

        if (d1 < 0.0D) {
            d4 += d1;
        } else if (d1 > 0.0D) {
            d7 += d1;
        }

        if (d2 < 0.0D) {
            d5 += d2;
        } else if (d2 > 0.0D) {
            d8 += d2;
        }

        return new AxisAlignedBB(d3, d4, d5, d6, d7, d8);
    }

    public AxisAlignedBB grow(double d0, double d1, double d2) {
        double d3 = this.a - d0;
        double d4 = this.b - d1;
        double d5 = this.c - d2;
        double d6 = this.d + d0;
        double d7 = this.e + d1;
        double d8 = this.f + d2;

        return new AxisAlignedBB(d3, d4, d5, d6, d7, d8);
    }

    public AxisAlignedBB g(double d0) {
        return this.grow(d0, d0, d0);
    }

    public AxisAlignedBB a(AxisAlignedBB axisalignedbb) {
        double d0 = Math.max(this.a, axisalignedbb.a);
        double d1 = Math.max(this.b, axisalignedbb.b);
        double d2 = Math.max(this.c, axisalignedbb.c);
        double d3 = Math.min(this.d, axisalignedbb.d);
        double d4 = Math.min(this.e, axisalignedbb.e);
        double d5 = Math.min(this.f, axisalignedbb.f);

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB b(AxisAlignedBB axisalignedbb) {
        double d0 = Math.min(this.a, axisalignedbb.a);
        double d1 = Math.min(this.b, axisalignedbb.b);
        double d2 = Math.min(this.c, axisalignedbb.c);
        double d3 = Math.max(this.d, axisalignedbb.d);
        double d4 = Math.max(this.e, axisalignedbb.e);
        double d5 = Math.max(this.f, axisalignedbb.f);

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB d(double d0, double d1, double d2) {
        return new AxisAlignedBB(this.a + d0, this.b + d1, this.c + d2, this.d + d0, this.e + d1, this.f + d2);
    }

    public AxisAlignedBB a(BlockPosition blockposition) {
        return new AxisAlignedBB(this.a + (double) blockposition.getX(), this.b + (double) blockposition.getY(), this.c + (double) blockposition.getZ(), this.d + (double) blockposition.getX(), this.e + (double) blockposition.getY(), this.f + (double) blockposition.getZ());
    }

    public AxisAlignedBB a(Vec3D vec3d) {
        return this.d(vec3d.x, vec3d.y, vec3d.z);
    }

    public double a(AxisAlignedBB axisalignedbb, double d0) {
        if (axisalignedbb.e > this.b && axisalignedbb.b < this.e && axisalignedbb.f > this.c && axisalignedbb.c < this.f) {
            double d1;

            if (d0 > 0.0D && axisalignedbb.d <= this.a) {
                d1 = this.a - axisalignedbb.d;
                if (d1 < d0) {
                    d0 = d1;
                }
            } else if (d0 < 0.0D && axisalignedbb.a >= this.d) {
                d1 = this.d - axisalignedbb.a;
                if (d1 > d0) {
                    d0 = d1;
                }
            }

            return d0;
        } else {
            return d0;
        }
    }

    public double b(AxisAlignedBB axisalignedbb, double d0) {
        if (axisalignedbb.d > this.a && axisalignedbb.a < this.d && axisalignedbb.f > this.c && axisalignedbb.c < this.f) {
            double d1;

            if (d0 > 0.0D && axisalignedbb.e <= this.b) {
                d1 = this.b - axisalignedbb.e;
                if (d1 < d0) {
                    d0 = d1;
                }
            } else if (d0 < 0.0D && axisalignedbb.b >= this.e) {
                d1 = this.e - axisalignedbb.b;
                if (d1 > d0) {
                    d0 = d1;
                }
            }

            return d0;
        } else {
            return d0;
        }
    }

    public double c(AxisAlignedBB axisalignedbb, double d0) {
        if (axisalignedbb.d > this.a && axisalignedbb.a < this.d && axisalignedbb.e > this.b && axisalignedbb.b < this.e) {
            double d1;

            if (d0 > 0.0D && axisalignedbb.f <= this.c) {
                d1 = this.c - axisalignedbb.f;
                if (d1 < d0) {
                    d0 = d1;
                }
            } else if (d0 < 0.0D && axisalignedbb.c >= this.f) {
                d1 = this.f - axisalignedbb.c;
                if (d1 > d0) {
                    d0 = d1;
                }
            }

            return d0;
        } else {
            return d0;
        }
    }

    public boolean c(AxisAlignedBB axisalignedbb) {
        return this.a(axisalignedbb.a, axisalignedbb.b, axisalignedbb.c, axisalignedbb.d, axisalignedbb.e, axisalignedbb.f);
    }

    public boolean a(double d0, double d1, double d2, double d3, double d4, double d5) {
        return this.a < d3 && this.d > d0 && this.b < d4 && this.e > d1 && this.c < d5 && this.f > d2;
    }

    public boolean b(Vec3D vec3d) {
        return vec3d.x > this.a && vec3d.x < this.d ? (vec3d.y > this.b && vec3d.y < this.e ? vec3d.z > this.c && vec3d.z < this.f : false) : false;
    }

    public double a() {
        double d0 = this.d - this.a;
        double d1 = this.e - this.b;
        double d2 = this.f - this.c;

        return (d0 + d1 + d2) / 3.0D;
    }

    public AxisAlignedBB shrink(double d0) {
        return this.g(-d0);
    }

    @Nullable
    public MovingObjectPosition b(Vec3D vec3d, Vec3D vec3d1) {
        Vec3D vec3d2 = this.a(this.a, vec3d, vec3d1);
        EnumDirection enumdirection = EnumDirection.WEST;
        Vec3D vec3d3 = this.a(this.d, vec3d, vec3d1);

        if (vec3d3 != null && this.a(vec3d, vec3d2, vec3d3)) {
            vec3d2 = vec3d3;
            enumdirection = EnumDirection.EAST;
        }

        vec3d3 = this.b(this.b, vec3d, vec3d1);
        if (vec3d3 != null && this.a(vec3d, vec3d2, vec3d3)) {
            vec3d2 = vec3d3;
            enumdirection = EnumDirection.DOWN;
        }

        vec3d3 = this.b(this.e, vec3d, vec3d1);
        if (vec3d3 != null && this.a(vec3d, vec3d2, vec3d3)) {
            vec3d2 = vec3d3;
            enumdirection = EnumDirection.UP;
        }

        vec3d3 = this.c(this.c, vec3d, vec3d1);
        if (vec3d3 != null && this.a(vec3d, vec3d2, vec3d3)) {
            vec3d2 = vec3d3;
            enumdirection = EnumDirection.NORTH;
        }

        vec3d3 = this.c(this.f, vec3d, vec3d1);
        if (vec3d3 != null && this.a(vec3d, vec3d2, vec3d3)) {
            vec3d2 = vec3d3;
            enumdirection = EnumDirection.SOUTH;
        }

        return vec3d2 == null ? null : new MovingObjectPosition(vec3d2, enumdirection);
    }

    @VisibleForTesting
    boolean a(Vec3D vec3d, @Nullable Vec3D vec3d1, Vec3D vec3d2) {
        return vec3d1 == null || vec3d.distanceSquared(vec3d2) < vec3d.distanceSquared(vec3d1);
    }

    @Nullable
    @VisibleForTesting
    Vec3D a(double d0, Vec3D vec3d, Vec3D vec3d1) {
        Vec3D vec3d2 = vec3d.a(vec3d1, d0);

        return vec3d2 != null && this.c(vec3d2) ? vec3d2 : null;
    }

    @Nullable
    @VisibleForTesting
    Vec3D b(double d0, Vec3D vec3d, Vec3D vec3d1) {
        Vec3D vec3d2 = vec3d.b(vec3d1, d0);

        return vec3d2 != null && this.d(vec3d2) ? vec3d2 : null;
    }

    @Nullable
    @VisibleForTesting
    Vec3D c(double d0, Vec3D vec3d, Vec3D vec3d1) {
        Vec3D vec3d2 = vec3d.c(vec3d1, d0);

        return vec3d2 != null && this.e(vec3d2) ? vec3d2 : null;
    }

    @VisibleForTesting
    public boolean c(Vec3D vec3d) {
        return vec3d.y >= this.b && vec3d.y <= this.e && vec3d.z >= this.c && vec3d.z <= this.f;
    }

    @VisibleForTesting
    public boolean d(Vec3D vec3d) {
        return vec3d.x >= this.a && vec3d.x <= this.d && vec3d.z >= this.c && vec3d.z <= this.f;
    }

    @VisibleForTesting
    public boolean e(Vec3D vec3d) {
        return vec3d.x >= this.a && vec3d.x <= this.d && vec3d.y >= this.b && vec3d.y <= this.e;
    }

    public String toString() {
        return "box[" + this.a + ", " + this.b + ", " + this.c + " -> " + this.d + ", " + this.e + ", " + this.f + "]";
    }
}
