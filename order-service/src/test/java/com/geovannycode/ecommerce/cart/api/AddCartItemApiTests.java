package com.geovannycode.ecommerce.cart.api;

import com.geovannycode.ecommerce.common.AbstractIT;
import org.junit.jupiter.api.Disabled;

@Disabled
// @SpringBootTest(classes = OrderServiceApplication.class)
public class AddCartItemApiTests extends AbstractIT {

    // @Autowired
    // private CartRepository cartRepository;
    /*
    @Test
    void shouldAddItemToNewCart() {
        mockGetProductByCode("P100", "Product 1", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
        given().contentType(ContentType.JSON)
                .body(
                        """
                        {
                            "code": "P100",
                            "quantity": 2
                        }
                        """)
                .when()
                .post("/api/carts")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("items", hasSize(1));
    }
    /*
       @Test
       void shouldAddItemToExistingCart() {
           mockGetProductByCode("P100", "Product 1", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
           String cartId = UUID.randomUUID().toString();
           cartRepository.save(new Cart(cartId, Set.of()));
           given().contentType(ContentType.JSON)
                   .body("""
                           {
                               "code": "P100",
                               "quantity": 2
                           }
                           """)
                   .when()
                   .post("/api/carts?cartId={cartId}", cartId)
                   .then()
                   .statusCode(200)
                   .body("id", is(cartId))
                   .body("items", hasSize(1))
                   .body("items[0].code", is("P100"))
                   .body("items[0].quantity", is(2));
       }

       @Test
       void shouldGetNotFoundWhenAddItemToNonExistingCart() {
           given().contentType(ContentType.JSON)
                   .body("""
                           {
                               "code": "P100",
                               "quantity": 2
                           }
                           """)
                   .when()
                   .post("/api/carts?cartId={cartId}", "non-existing-cart-id")
                   .then()
                   .statusCode(404);
       }

       @Test
       void shouldGetNotFoundWhenAddInvalidItemToCart() {
           given().contentType(ContentType.JSON)
                   .body("""
                           {
                               "code": "non-existing-product-id",
                               "quantity": 2
                           }
                           """)
                   .when()
                   .post("/api/carts")
                   .then()
                   .statusCode(404);
       }

       @Test
       void shouldAddItemIncreaseQuantityWhenAddingSameProduct() {
           mockGetProductByCode("P100", "Product 1", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
           String cartId = UUID.randomUUID().toString();
           cartRepository.save(
                   new Cart(
                           cartId,
                           Set.of(new CartItem("P100", "Product 1", "P100 desc", BigDecimal.TEN, 2))));
           given().contentType(ContentType.JSON)
                   .body("""
                           {
                               "code": "P100",
                               "quantity": 1
                           }
                           """)
                   .when()
                   .post("/api/carts?cartId={cartId}", cartId)
                   .then()
                   .statusCode(200)
                   .body("id", is(cartId))
                   .body("items", hasSize(1))
                   .body("items[0].code", is("P100"))
                   .body("items[0].quantity", is(3));
       }

       @Test
       void shouldAddDifferentProduct() {
           mockGetProductByCode("P101", "Product 2", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
           String cartId = UUID.randomUUID().toString();
           cartRepository.save(
                   new Cart(
                           cartId,
                           Set.of(new CartItem("P100", "Product 1", "P100 desc", BigDecimal.TEN, 2))));
           given().contentType(ContentType.JSON)
                   .body("""
                           {
                               "code": "P101",
                               "quantity": 1
                           }
                           """)
                   .when()
                   .post("/api/carts?cartId={cartId}", cartId)
                   .then()
                   .statusCode(200)
                   .body("id", is(cartId))
                   .body("items", hasSize(2));
       }
    */
}
