declare namespace API {
  type BaseResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseInt_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseListSpaceLevel_ = {
    code?: number
    data?: SpaceLevel[]
    message?: string
  }

  type BaseResponseListSpaceUserVO_ = {
    code?: number
    data?: SpaceUserVO[]
    message?: string
  }

  type BaseResponseListSpaceVO_ = {
    code?: number
    data?: SpaceVO[]
    message?: string
  }

  type BaseResponseLoginUserVO_ = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePagePicture_ = {
    code?: number
    data?: PagePicture_
    message?: string
  }

  type BaseResponsePagePictureVO_ = {
    code?: number
    data?: PagePictureVO_
    message?: string
  }

  type BaseResponsePageSpace_ = {
    code?: number
    data?: PageSpace_
    message?: string
  }

  type BaseResponsePageSpaceVO_ = {
    code?: number
    data?: PageSpaceVO_
    message?: string
  }

  type BaseResponsePageUserVO_ = {
    code?: number
    data?: PageUserVO_
    message?: string
  }

  type BaseResponsePicture_ = {
    code?: number
    data?: Picture
    message?: string
  }

  type BaseResponsePictureTagCategory_ = {
    code?: number
    data?: PictureTagCategory
    message?: string
  }

  type BaseResponsePictureVO_ = {
    code?: number
    data?: PictureVO
    message?: string
  }

  type BaseResponseSpace_ = {
    code?: number
    data?: Space
    message?: string
  }

  type BaseResponseSpaceUser_ = {
    code?: number
    data?: SpaceUser
    message?: string
  }

  type BaseResponseSpaceVO_ = {
    code?: number
    data?: SpaceVO
    message?: string
  }

  type BaseResponseUser_ = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO_ = {
    code?: number
    data?: UserVO
    message?: string
  }

  type DeleteRequest = {
    id?: string
  }

  type getPictureByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type getPictureVOByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type getSpaceByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type getSpaceVOByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type getUserByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type LoginUserVO = {
    createTime?: string
    editTime?: string
    id?: string
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type PagePicture_ = {
    current?: number
    pages?: number
    records?: Picture[]
    size?: number
    total?: number
  }

  type PagePictureVO_ = {
    current?: number
    pages?: number
    records?: PictureVO[]
    size?: number
    total?: number
  }

  type PageSpace_ = {
    current?: number
    pages?: number
    records?: Space[]
    size?: number
    total?: number
  }

  type PageSpaceVO_ = {
    current?: number
    pages?: number
    records?: SpaceVO[]
    size?: number
    total?: number
  }

  type PageUserVO_ = {
    current?: number
    pages?: number
    records?: UserVO[]
    size?: number
    total?: number
  }

  type Picture = {
    category?: string
    createTime?: string
    editTime?: string
    hash?: string
    id?: string
    introduction?: string
    isDelete?: number
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: string
    spaceId?: string
    tags?: string
    updateTime?: string
    url?: string
    userId?: string
  }

  type PictureQueryRequest = {
    category?: string
    current?: number
    id?: string
    introduction?: string
    name?: string
    pageSize?: number
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    publicFlag?: boolean
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: string
    searchText?: string
    sortField?: string
    sortOrder?: string
    spaceId?: string
    tags?: string[]
    userId?: string
  }

  type PictureReviewRequest = {
    current?: number
    id?: string
    pageSize?: number
    reviewMessage?: string
    reviewStatus?: number
    sortField?: string
    sortOrder?: string
  }

  type PictureTagCategory = {
    categoryList?: string[]
    tagList?: string[]
  }

  type PictureUpdateRequest = {
    category?: string
    id?: string
    introduction?: string
    name?: string
    tags?: string[]
  }

  type PictureUploadByBatchRequest = {
    count?: number
    namePrefix?: string
    searchText?: string
  }

  type PictureUploadRequest = {
    fileUrl?: string
    id?: string
    picName?: string
    spaceId?: string
  }

  type PictureVO = {
    category?: string
    createTime?: string
    editTime?: string
    id?: string
    introduction?: string
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    spaceId?: string
    tags?: string[]
    updateTime?: string
    url?: string
    userId?: string
    userVO?: UserVO
  }

  type Space = {
    createTime?: string
    editTime?: string
    id?: string
    isDelete?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    totalCount?: number
    totalSize?: number
    updateTime?: string
    userId?: string
  }

  type SpaceAddRequest = {
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
  }

  type SpaceEditRequest = {
    id?: string
    spaceName?: string
  }

  type SpaceLevel = {
    maxCount?: number
    maxSize?: number
    text?: string
    value?: number
  }

  type SpaceQueryRequest = {
    current?: number
    id?: string
    pageSize?: number
    sortField?: string
    sortOrder?: string
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    userId?: string
  }

  type SpaceUpdateRequest = {
    id?: string
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceUser = {
    createTime?: string
    id?: string
    spaceId?: string
    spaceRole?: string
    updateTime?: string
    userId?: string
  }

  type SpaceUserAddRequest = {
    spaceId?: string
    spaceRole?: string
    userId?: string
  }

  type SpaceUserEditRequest = {
    id?: string
    spaceRole?: string
  }

  type SpaceUserQueryRequest = {
    id?: string
    spaceId?: string
    spaceRole?: string
    userId?: string
  }

  type SpaceUserVO = {
    createTime?: string
    id?: string
    space?: SpaceVO
    spaceId?: string
    spaceRole?: string
    updateTime?: string
    user?: UserVO
    userId?: string
  }

  type SpaceVO = {
    createTime?: string
    editTime?: string
    id?: string
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    totalCount?: number
    totalSize?: number
    updateTime?: string
    user?: UserVO
    userId?: string
  }

  type uploadPictureUsingPOSTParams = {
    fileUrl?: string
    id?: string
    picName?: string
    spaceId?: string
  }

  type User = {
    createTime?: string
    editTime?: string
    id?: string
    isDelete?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
  }

  type UserAddRequest = {
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    id?: string
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userAccount?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    checkPassword?: string
    userAccount?: string
    userPassword?: string
  }

  type UserUpdateRequest = {
    id?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    id?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
}
