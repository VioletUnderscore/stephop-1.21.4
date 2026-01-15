package net.violetunderscore.stephop.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.nerdorg.minehop.config.MinehopConfig;
import net.violetunderscore.stephop.config.ConfigWrapper;
import net.violetunderscore.stephop.config.StephopConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public boolean noClip;
    @Shadow private boolean onFire;
    @Shadow public abstract boolean isOnFire();
    @Shadow public abstract void setPosition(double x, double y, double z);
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow protected abstract Vec3d adjustMovementForPiston(Vec3d movement);
    @Shadow protected Vec3d movementMultiplier;
    @Shadow public abstract void setVelocity(Vec3d velocity);
    @Shadow protected abstract Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type);
    @Shadow public float fallDistance;
    @Shadow public abstract World getWorld();
    @Shadow public abstract void onLanding();
    @Shadow public boolean horizontalCollision;
    @Shadow public abstract boolean isLocalPlayerOrLogicalSideForUpdatingMovement();
    @Shadow public boolean verticalCollision;
    @Shadow public boolean groundCollision;
    @Shadow public abstract void setMovement(boolean onGround, boolean horizontalCollision, Vec3d movement);
    @Shadow public boolean collidedSoftly;
    @Shadow protected abstract boolean hasCollidedSoftly(Vec3d adjustedMovement);
    @Shadow @Deprecated public abstract BlockPos getLandingPos();
    @Shadow public abstract boolean isLogicalSideForUpdatingMovement();
    @Shadow public abstract boolean isOnGround();
    @Shadow protected abstract void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);
    @Shadow public abstract boolean isRemoved();
    @Shadow public abstract Vec3d getVelocity();
    @Shadow public abstract void setVelocity(double x, double y, double z);
    @Shadow public abstract boolean isControlledByPlayer();
    @Shadow protected abstract Entity.MoveEffect getMoveEffect();
    @Shadow public abstract boolean hasVehicle();
    @Shadow protected abstract void applyMoveEffect(Entity.MoveEffect moveEffect, Vec3d movement, BlockPos landingPos, BlockState landingState);
    @Shadow protected abstract float getVelocityMultiplier();
    @Shadow public abstract int getId();
    @Shadow public abstract Vec3d getPos();
    @Shadow public abstract Box getBoundingBox();
    @Shadow public abstract float getStepHeight();
    @Shadow protected static List<VoxelShape> findCollisionsForMovement(@Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox) {return null;}
    @Shadow private World world;
    @Shadow protected static float[] collectStepHeights(Box collisionBox, List<VoxelShape> collisions, float f, float stepHeight) {return null;}
    @Shadow protected static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, List<VoxelShape> collisions) {return null;}
    @Shadow public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions) {return null;}
    @Shadow public abstract boolean isSneaking();

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MovementType type, Vec3d movement, CallbackInfo ci) {
        Entity self = this.getWorld().getEntityById(this.getId());
        if (!MinehopConfig.enabled) { return; }
        if (!(self instanceof PlayerEntity p)) { return; }

        if (this.noClip) {
            this.setPosition(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        } else {
            this.onFire = this.isOnFire();
            if (type == MovementType.PISTON) {
                movement = this.adjustMovementForPiston(movement);
                if (movement.equals(Vec3d.ZERO)) {
                    return;
                }
            }

            Profiler profiler = Profilers.get();
            profiler.push("move");
            if (this.movementMultiplier.lengthSquared() > 1.0E-7) {
                movement = movement.multiply(this.movementMultiplier);
                this.movementMultiplier = Vec3d.ZERO;
                this.setVelocity(Vec3d.ZERO);
            }

            movement = this.adjustMovementForSneaking(movement, type);
            Vec3d vec3d = this.adjustMovementForCollisions(movement);
            double d = vec3d.lengthSquared();
            if (d > 1.0E-7 || movement.lengthSquared() - d < 1.0E-7) {
                if (this.fallDistance != 0.0F && d >= (double)1.0F) {
                    BlockHitResult blockHitResult = this.getWorld().raycast(new RaycastContext(this.getPos(), this.getPos().add(vec3d), RaycastContext.ShapeType.FALLDAMAGE_RESETTING, RaycastContext.FluidHandling.WATER, self));
                    if (blockHitResult.getType() != HitResult.Type.MISS) {
                        this.onLanding();
                    }
                }

                this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
            }

            profiler.pop();
            profiler.push("rest");
            boolean bl = !MathHelper.approximatelyEquals(movement.x, vec3d.x);
            boolean bl2 = !MathHelper.approximatelyEquals(movement.z, vec3d.z);
            this.horizontalCollision = bl || bl2;
            if (Math.abs(movement.y) > (double)0.0F || this.isLocalPlayerOrLogicalSideForUpdatingMovement()) {
                this.verticalCollision = movement.y != vec3d.y;
                this.groundCollision = this.verticalCollision && movement.y < (double)0.0F;
                this.setMovement(this.groundCollision, this.horizontalCollision, vec3d);
            }

            if (this.horizontalCollision) {
                this.collidedSoftly = this.hasCollidedSoftly(vec3d);
            } else {
                this.collidedSoftly = false;
            }

            BlockPos blockPos = this.getLandingPos();
            BlockState blockState = this.getWorld().getBlockState(blockPos);
            if ((!this.getWorld().isClient() || this.isLogicalSideForUpdatingMovement()) && !this.isControlledByPlayer()) {
                this.fall(vec3d.y, this.isOnGround(), blockState, blockPos);
            }

            if (this.isRemoved()) {
                profiler.pop();
            } else {
                if (this.horizontalCollision) {
                    Vec3d vec3d2 = this.getVelocity();
                    if (!(this.isSneaking() && MinehopConfig.enabled)) {
                        this.setVelocity(bl ? (double) 0.0F : vec3d2.x, vec3d2.y, bl2 ? (double) 0.0F : vec3d2.z);
                    } else {
                        this.setVelocity(bl ? Math.max((double) 0.0F, vec3d2.x - 0.2) : vec3d2.x, vec3d2.y, bl2 ? Math.max((double) 0.0F, vec3d2.z - 0.2) : vec3d2.z);
                    }
                }

                if (this.isLogicalSideForUpdatingMovement()) {
                    Block block = blockState.getBlock();
                    if (movement.y != vec3d.y) {
                        block.onEntityLand(this.getWorld(), self);
                    }
                }

                if (!this.getWorld().isClient() || this.isLogicalSideForUpdatingMovement()) {
                    Entity.MoveEffect moveEffect = this.getMoveEffect();
                    if (moveEffect.hasAny() && !this.hasVehicle()) {
                        this.applyMoveEffect(moveEffect, vec3d, blockPos, blockState);
                    }
                }

                float f = this.getVelocityMultiplier();
                this.setVelocity(this.getVelocity().multiply((double)f, (double)1.0F, (double)f));
                profiler.pop();
            }
        }

        ci.cancel();
    }

    @Unique
    private Vec3d adjustMovementForCollisions(Vec3d movement) {
        StephopConfig config = ConfigWrapper.config;
        MinehopConfig minehopConfig = net.nerdorg.minehop.config.ConfigWrapper.config;

        Entity self = (Entity) this.getWorld().getEntityById(this.getId());

        Box box = this.getBoundingBox();
        List<VoxelShape> list = this.getWorld().getEntityCollisions(self, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == (double)0.0F ? movement : adjustMovementForCollisions(self, movement, box, this.getWorld(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = bl2 && movement.y < (double)0.0F;

        float newStepHeight = this.getStepHeight();
        boolean b = false;
        if ((this.isSneaking() || !config.require_crouching) && minehopConfig.enabled) {
            newStepHeight = Math.max(newStepHeight, config.step_height);
            b = true;
        }

        if (this.getStepHeight() > 0.0F
                && ((bl4 || this.isOnGround()) || b)
                && (bl || bl3)) {
            Box box2 = bl4 ? box.offset((double)0.0F, vec3d.y, (double)0.0F) : box;
            Box box3 = box2.stretch(movement.x, newStepHeight, movement.z);
            if (!bl4) {
                box3 = box3.stretch((double)0.0F, (double)-1.0E-5F, (double)0.0F);
            }

            List<VoxelShape> list2 = findCollisionsForMovement(self, this.world, list, box3);
            float f = (float)vec3d.y;
            float[] fs = collectStepHeights(box2, list2, newStepHeight, f);

            for(float g : fs) {
                Vec3d vec3d2 = adjustMovementForCollisions(new Vec3d(movement.x, (double)g, movement.z), box2, list2);
                if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                    double d = box.minY - box2.minY;
                    return vec3d2.add((double)0.0F, -d, (double)0.0F);
                }
            }
        }

        return vec3d;
    }
}