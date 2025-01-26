/*
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package labs.pm.data;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

/**
 * {@code Product} class represents properties and behaviors of
 * product objects in the Product Management System.
 * <br>
 * Each product has an id, name, and price
 * <br>
 * Each product can have a discount, calculated based on a
 * {@link DISCOUNT_RATE discount rate}
 * @version 1.0
 * @author Ayman ElMikh
 */

public class Product {

    public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);

    private final int id;
    private final String name;
    private final BigDecimal price;
    private final Rating rating;

    public Product(int id, String name, BigDecimal price, Rating rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    public Product(int id, String name, BigDecimal price) {
        this(id, name, price, Rating.NOT_RATED);
    }

    public Product(){
        this(0,"np name", BigDecimal.ZERO);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Calculates and returns the discount for the product.
     * <p>
     * The discount is calculated by multiplying the product's price with the
     * {@code DISCOUNT_RATE}. The result is rounded to two decimal places using
     * the {@code HALF_UP} rounding mode.
     * </p>
     *
     * @return the discount amount as a {@code BigDecimal}, rounded to two decimal places
     */

    public BigDecimal getDiscount(){
        return  price.multiply(DISCOUNT_RATE).setScale(2, HALF_UP);
    }

    public Rating getRating(){
        return this.rating;
    }

    public Product applyRating(final Rating newRating){
        return new Product(id, name, price, newRating);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", rating =" + rating.getStars() +
                '}';
    }



}
