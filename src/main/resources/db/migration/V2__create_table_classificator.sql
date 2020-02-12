create table classificator
(
    HASH     CHAR(44) PRIMARY KEY,
    KEYWORD  TEXT,
    CATEGORY TEXT
);
INSERT INTO classificator(HASH, KEYWORD, CATEGORY)
VALUES ('eqXrRypVYuOwIbVCG/r/PCPv8LKzjUkka6/eFtY354k=', 'grocery', 'grocery'),
       ('YkhOIqalreG6JcsbfFXEuIYd4kyt2rc8lAl0JzQAiyY=', 'health', 'health'),
       ('WctCJG3QQqzgmuwrjBDYR7lJauo/NSkpd1IFv6inSMI=', 'selfcare', 'selfcare'),
       ('AglELhFa17x5/SgdkUI6hrYZ48cR/ldLfMGY0uPEYcQ=', 'travel', 'travel'),
       ('87t55Ip6ivHQKPast/o0sMf3XaWW/CBZB9KnCP4o6+8=', 'lunch', 'lunch'),
       ('hiQXuefDcgvLMmPNhzsJiS14eCO2+aD0U+QoJMWk1LY=', 'events', 'events'),
       ('IfqxBb758YMMnE9fmdqkQCaa/JY0dskzQzDoD3lAgvA=', 'gifts', 'gifts'),
       ('XF+bh3eM+xA6oD7CWFOY6agEnaVVJZFgnvzXhK2E2Ec=', 'clothes', 'clothes'),
       ('Dp/RWWxLHfyo3c02xWfxHaQ88XlopbkS8OZ8UZj2Q/E=', 'rent', 'rent')