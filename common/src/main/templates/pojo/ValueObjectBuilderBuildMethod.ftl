    public ${pojo.getDeclarationName()}VO build() {
        return new ${pojo.getDeclarationName()}VO(${c2j.asVOArgumentList(pojo.getPropertyClosureForFullConstructor(true))});
    }