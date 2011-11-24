/*
 * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.jme.intersection;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Line;
import com.jme.math.Plane;
import com.jme.math.Ray;
import com.jme.math.Vector3f;

/**
 * <code>Intersection</code> provides functional methods for calculating the
 * intersection of sphere objects. All the methods are static to allow for quick
 * and easy calls.
 * @author Mark Powell
 * @version $Id: IntersectionSphere.java,v 1.1 2004-04-02 16:27:31 mojomonkey Exp $
 */
public class IntersectionSphere {
	
	/**
    *
    * <code>intersection</code> determines if a ray has intersected a sphere.
    * @param ray the ray to test.
    * @param sphere the sphere to test.
    * @return true if they intersect, false otherwise.
    */
   public static boolean intersection(Ray ray, BoundingSphere sphere) {
       Vector3f diff = ray.getOrigin().subtract(sphere.getCenter());
       float a = ray.getDirection().lengthSquared();
       float b = diff.dot(ray.getDirection());
       float c =
           diff.lengthSquared() - sphere.getRadius() * sphere.getRadius();

       float t[] = new float[2];
       float discr = b * b - a * c;
       if (discr < 0.0) {
           return false;
       } else if (discr > 0.0) {
           float root = (float) Math.sqrt(discr);
           float invA = 1.0f / a;
           t[0] = (-b - root) * invA;
           t[1] = (-b + root) * invA;

           if (t[0] >= 0.0) {
               return true;
           } else if (t[1] >= 0.0) {
               return true;
           } else {
               return false;
           }
       } else {
           t[0] = -b / a;
           if (t[0] >= 0.0) {
               return true;
           } else {
               return false;
           }
       }
   }


   /**
    *
    * <code>intersection</code> compares a dynamic sphere to a stationary line.
    * The velocity of the sphere is given as well as the period of time for
    * movement. If a collision occurs somewhere along this time period, true
    * is returned. False is returned otherwise.
    * @param line the stationary line to test against.
    * @param sphere the dynamic sphere to test.
    * @param velocity the velocity of the sphere.
    * @param time the time range to test.
    * @return true if intersection occurs, false otherwise.
    */
   public static boolean intersection(
       Line line,
       BoundingSphere sphere,
       Vector3f velocity,
       float time) {

       Vector3f e = sphere.getCenter().subtract(line.getOrigin());
       float dotDW = line.getDirection().dot(velocity);
       float dotDD = line.getDirection().dot(line.getDirection());
       float dotWW = velocity.dot(velocity);
       float dotWE = velocity.dot(e);
       float dotDE = line.getDirection().dot(e);
       float dotEE = e.dot(e);
       float ddr2 = dotDD * sphere.getRadius() * sphere.getRadius();

       float a = dotDD * dotWW - dotDW * dotDW;
       float b = dotDD * dotWE - dotDE * dotDW;
       float c = dotDD * dotEE - dotDE * dotDE;

       if (a > 0) {
           float t = -b / a;
           if (t < 0) {
               return c <= ddr2;
           } else if (t > time) {
               return time * (a * time + 2 * b) + c <= ddr2;
           } else {
               return t * (a * t + 2 * b) + c <= ddr2;
           }
       } else {
           return c <= ddr2;
       }
   }

   /**
    *
    * <code>intersection</code> compares a dynamix sphere to a stationary plane.
    * The velocity of the sphere is given as well as the period of time for
    * movement. If a collision occurs somewhere along this time period, true is
    * returned. False is returned otherwise.
    * @param plane the stationary plane to test against.
    * @param sphere the dynamic sphere to test.
    * @param velocity the velocity of the sphere.
    * @param time the time range to test.
    * @return true if intersection occurs, false otherwise.
    */
   public static boolean intersection(
       Plane plane,
       BoundingSphere sphere,
       Vector3f velocity,
       float time) {

       float sdist =
           plane.getNormal().dot(sphere.getCenter()) - plane.getConstant();

       if (sdist > sphere.getRadius()) {
           float dotNW = plane.getNormal().dot(velocity);
           return (sphere.getRadius() - sdist / dotNW) < time;
       } else if (sdist < -sphere.getRadius()) {
           float dotNW = plane.getNormal().dot(velocity);
           return (- (sphere.getRadius() + sdist) / dotNW) < time;
       } else {
           return true;
       }
   }
   
   /**
   *
   * <code>intersection</code> compares two static spheres for intersection.
   * If any part of the two spheres touch, true is returned, otherwise false
   * will return.
   * @param sphere1 the first sphere to test.
   * @param sphere2 the second sphere to test.
   * @return true if the spheres are intersecting, false otherwise.
   */
  public static boolean intersection(
      BoundingSphere sphere1,
      BoundingSphere sphere2) {
      Vector3f diff = sphere1.getCenter().subtract(sphere2.getCenter());
      float rsum = sphere1.getRadius() + sphere2.getRadius();
      return (diff.dot(diff) <= rsum * rsum);
  }

  /**
   *
   * <code>intersection</code> compares two dynamic spheres. Both sphers have
   * a velocity and a time is givin to check for. If these spheres will
   * collide within the time alloted, true is returned, otherwise false is
   * returned.
   * @param sphere1 the first sphere to test.
   * @param sphere2 the second sphere to test.
   * @param velocity1 the velocity of the first sphere.
   * @param velocity2 the velocity of the second sphere.
   * @param time the time frame to check.
   * @return true if a collision occurs, false otherwise.
   */
  public static boolean intersection(
      BoundingSphere sphere1,
      BoundingSphere sphere2,
      Vector3f velocity1,
      Vector3f velocity2,
      float time) {

      Vector3f velocityDiff = velocity2.subtract(velocity1);
      float a = velocityDiff.lengthSquared();
      Vector3f kCDiff = sphere2.getCenter().subtract(sphere1.getCenter());
      float c = kCDiff.lengthSquared();
      float radiusSum = sphere1.getRadius() + sphere2.getRadius();
      float radiusSumSquared = radiusSum * radiusSum;

      if (a > 0.0) {
          float b = kCDiff.dot(velocityDiff);
          if (b <= 0.0) {
              if (-time * a <= b)
                  return a * c - b * b <= a * radiusSumSquared;
              else
                  return time * (time * a + 2.0 * b) + c <= radiusSumSquared;
          }
      }

      return c <= radiusSumSquared;
  }

}