package com.foodstagram.controller.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({ValidationGroups.NotBlankGroup.class, ValidationGroups.PatternGroup.class})
public interface ValidationSequence {
}
