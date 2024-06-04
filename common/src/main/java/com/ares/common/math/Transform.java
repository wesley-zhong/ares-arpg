package com.ares.common.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Transform {
    private Vector3 position;
    private Vector3 rotation;
}
