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

package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.Rating;

import java.math.BigDecimal;


/**
 * {@code Shop} class represents an application that manages Products
 * @version 1.0
 * @author AymanElMikh
 */
public class Shop {
    public static void main(String[] args) {
        Product p1 = new Product(1, "Shoes", BigDecimal.valueOf(131.2));
        System.out.println(p1);

        System.out.println("------------------------");
        Product p2 = new Product(2, "Cake", BigDecimal.valueOf(11.2), Rating.FIVE_STAR);
        System.out.println(p2);

    }

}